/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cassandra.transport.messages;

import java.util.UUID;

import com.google.common.collect.ImmutableMap;

import io.netty.buffer.ByteBuf;
import org.apache.cassandra.cql3.QueryOptions;
import org.apache.cassandra.exceptions.RequestExecutionException;
import org.apache.cassandra.exceptions.RequestValidationException;
import org.apache.cassandra.service.ClientState;
import org.apache.cassandra.service.QueryState;
import org.apache.cassandra.tracing.Tracing;
import org.apache.cassandra.transport.CBUtil;
import org.apache.cassandra.transport.Message;
import org.apache.cassandra.transport.ProtocolException;
import org.apache.cassandra.transport.ProtocolVersion;
import org.apache.cassandra.utils.JVMStabilityInspector;
import org.apache.cassandra.utils.UUIDGen;

/**
 * DB write query
 */
public class WriteMessage extends QueryMessage
{
    public final static String DB = "demodb.test";
	
	public static final Message.Codec<QueryMessage> codec = new Message.Codec<QueryMessage>()
    {
        public QueryMessage decode(ByteBuf body, ProtocolVersion version)
        {
            String query = CBUtil.readLongString(body);
            return new QueryMessage(query, QueryOptions.codec.decode(body, version));
        }

        public void encode(QueryMessage msg, ByteBuf dest, ProtocolVersion version)
        {
            CBUtil.writeLongString(msg.query, dest);
            if (version == ProtocolVersion.V1)
                CBUtil.writeConsistencyLevel(msg.options.getConsistency(), dest);
            else
                QueryOptions.codec.encode(msg.options, dest, version);
        }

        public int encodedSize(QueryMessage msg, ProtocolVersion version)
        {
            int size = CBUtil.sizeOfLongString(msg.query);

            if (version == ProtocolVersion.V1)
            {
                size += CBUtil.sizeOfConsistencyLevel(msg.options.getConsistency());
            }
            else
            {
                size += QueryOptions.codec.encodedSize(msg.options, version);
            }
            return size;
        }
    };
    
    public final String cert;
    public final String id;

    public WriteMessage(String id, String cert, int expDate, int issueDate, QueryOptions options)
    {
        super(getQuery(id, cert, expDate, issueDate), options);
        this.cert = cert;
        this.id = id;
    }
    
    private static String getQuery(String id, String cert, int expDate, int issueDate) {
    	StringBuilder sb = new StringBuilder("INSERT INTO " + DB + " (id,cert,issuedate,expdate) VALUES (");
        sb.append("'").append(id).append("', ");
        sb.append("textAsBlob('").append(cert).append("'), ");
        sb.append(issueDate).append(", ");
        sb.append(expDate).append(");");
        return sb.toString();
    }

    public Message.Response execute(QueryState state, long queryStartNanoTime)
    {
        try
        {
            if (options.getPageSize() == 0)
                throw new ProtocolException("The page size cannot be 0");

            UUID tracingId = null;
            if (isTracingRequested())
            {
                tracingId = UUIDGen.getTimeUUID();
                state.prepareTracingSession(tracingId);
            }

            if (state.traceNextQuery())
            {
                state.createTracingSession();

                ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
                builder.put("query", query);
                if (options.getPageSize() > 0)
                    builder.put("page_size", Integer.toString(options.getPageSize()));
                if(options.getConsistency() != null)
                    builder.put("consistency_level", options.getConsistency().name());
                if(options.getSerialConsistency() != null)
                    builder.put("serial_consistency_level", options.getSerialConsistency().name());

                Tracing.instance.begin("Execute CQL3 query", state.getClientAddress(), builder.build());
            }

            Message.Response response = ClientState.getCQLQueryHandler().process(query, state, options, getCustomPayload(), queryStartNanoTime);
            if (options.skipMetadata() && response instanceof ResultMessage.Rows)
                ((ResultMessage.Rows)response).result.metadata.setSkipMetadata();

            if (tracingId != null)
                response.setTracingId(tracingId);

            return response;
        }
        catch (Exception e)
        {
            JVMStabilityInspector.inspectThrowable(e);
            if (!((e instanceof RequestValidationException) || (e instanceof RequestExecutionException)))
                logger.error("Unexpected error during query", e);
            return ErrorMessage.fromException(e);
        }
        finally
        {
            Tracing.instance.stopSession();
        }
    }

    @Override
    public String toString()
    {
        return "QUERY " + query + "[pageSize = " + options.getPageSize() + "]";
    }
}
