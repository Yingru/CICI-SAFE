package safe;

import exoplex.common.utils.SafeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class SafeAuthority {
  static Logger logger = LogManager.getLogger(SafeAuthority.class);

  static final String bearerRef = "bearerRef";

  static final String subject = "subject";

  static String exampleSafeServer = "128.194.6.138:7777";

  static final String postMakeIPTokenSet = "postMakeIPTokenSet";
  static final String postUserAclEntry = "postUserAclEntry";
  static final String postUserTagAclEntry = "postUserTagAclEntry";
  static final String postCustomerPolicy = "postCustomerPolicy";
  static final String updateTagSet = "updateTagSet";
  static final String postStandardSliceDefaultPrivilegeSet = "postStandardSliceDefaultPrivilegeSet";
  static final String updateSubjectSet = "updateSubjectSet";
  static final String postOwnPrefixPolicy = "postOwnPrefixPolicy";
  static final String postMAEndorsement = "postMAEndorsement";
  static final String postStitchPolicy = "postStitchPolicy";
  static final String postObjectTagSet = "postObjectTagSet";
  static final String postProjectMembership = "postProjectMembership";
  static final String postSAEndorsement = "postSAEndorsement";
  static final String authZByPID = "authZByPID";
  static final String createSlice = "createSlice";
  static final String authorizeStitchByPID = "authorizeStitchByPID";
  static final String authZByUserAttr = "authZByUserAttr";
  static final String postAssignTag = "postAssignTag";
  static final String postCustomerConnectionPolicy = "postCustomerConnectionPolicy";
  static final String postSliceTagAclEntry = "postSliceTagAclEntry";
  static final String postPIEndorsement = "postPIEndorsement";
  static final String postProjectIDAcl = "postProjectIDAcl";
  static final String postCPEndorsement = "postCPEndorsement";
  static final String authZByProjectAttr = "authZByProjectAttr";
  static final String postProjectSet = "postProjectSet";
  static final String postPAEndorsement = "postPAEndorsement";
  static final String authorizeStitchByUID = "authorizeStitchByUID";
  static final String postTagSet = "postTagSet";
  static final String postTagPrivilegePolicy = "postTagPrivilegePolicy";
  static final String createProject = "createProject";
  static final String postAclEntrySet = "postAclEntrySet";
  static final String postUserEndorsement = "postUserEndorsement";
  static final String postMemberSet = "postMemberSet";
  static final String authorizeOwnPrefix = "authorizeOwnPrefix";
  static final String authorizeStitchByProjectAttr = "authorizeStitchByProjectAttr";
  static final String passDelegation = "passDelegation";
  static final String authorizeStitchBySliceAttr = "authorizeStitchBySliceAttr";
  static final String postSliceControl = "postSliceControl";
  static final String postLinkTagSetToProject = "postLinkTagSetToProject";
  static final String postSliceSet = "postSliceSet";
  static final String authorizeStitchByUserAttr = "authorizeStitchByUserAttr";
  static final String createSliver = "createSliver";
  static final String postIdSet = "postIdSet";
  static final String postDlgToken = "postDlgToken";
  static final String postGrantTagPriv = "postGrantTagPriv";
  static final String postUpdateObjectTagSet = "postUpdateObjectTagSet";
  static final String postStandardSliceControlSet = "postStandardSliceControlSet";
  static final String postLinkTagSetToSlice = "postLinkTagSetToSlice";
  static final String postIPAllocate = "postIPAllocate";
  static final String whoami = "whoami";
  static final String postTagAclEntry = "postTagAclEntry";
  static final String postSubjectSet = "postSubjectSet";
  ;

  String safeServer = "128.194.6.138:7777";

  String sdxSlice;

  String sdxKeyFile;

  HashSet<String> reservedKeys = new HashSet<String>();

  ArrayList<String> clientSlices = new ArrayList<>();

  ArrayList<String> principals = new ArrayList<>();

  HashMap<String, String> principalMap = new HashMap<>();

  HashMap<String, String> sliceToken = new HashMap<>();

  HashMap<String, String> sliceKeyMap = new HashMap<>();

  HashMap<String, String> sliceScid = new HashMap<>();

  HashMap<String, String> subjectSet = new HashMap<String, String>();

  HashMap<String, String> sliceIpMap = new HashMap<>();

  String[] slices;

  public SafeAuthority(
    String safeServer,
    String sdxSlice,
    String sdxKey,
    List<String> clientSlices,
    HashMap<String, String> clientKeyMap,
    HashMap<String, String> clientIpMap
  ) {
    this.safeServer = safeServer;
    this.sdxSlice = sdxSlice;
    this.sdxKeyFile = sdxKey;
    sliceKeyMap.put(this.sdxSlice, this.sdxKeyFile);
    this.clientSlices.addAll(clientSlices);
    for(String key: clientKeyMap.keySet()){
      sliceKeyMap.put(key, clientKeyMap.get(key));
    }
    sliceIpMap = clientIpMap;
  }

  public void initGeniTrustBase(){
    addPrincipals();
    initPrincipals();
    String token;
    reservedKeys.add("geniroot");
    reservedKeys.add("key_p1");
    reservedKeys.add("key_p2");
    reservedKeys.add("key_p3");
    reservedKeys.add("key_p4");
    simpleEndorseMent(postMAEndorsement, "geniroot", "key_p1", "MA");
    simpleEndorseMent(postPAEndorsement, "geniroot", "key_p2", "PA");
    simpleEndorseMent(postSAEndorsement, "geniroot", "key_p3", "SA");

    String piCap = simpleEndorseMent(postPIEndorsement, "key_p1", "key_p4", "PI");

    for(int i=10; i<20; i++) {
      simpleEndorseMent(postUserEndorsement, "key_p1", "key_p" + i, "User");
    }
    simpleEndorseMent(postUserEndorsement, "key_p1", "sdx", "User");

    HashMap<String, String> envs = new HashMap<>();
    envs.put(subject, principalMap.get("key_p4"));
    envs.put(bearerRef, piCap);
    authorize(createProject, "key_p2", new String[]{}, envs);
    String paMemberSetRef = safePost(postMemberSet, "key_p2");
    String projectId = principalMap.get("key_p2") + ":project1";
    String projectToken = safePost(postProjectSet, "key_p2",
      new String[]{principalMap.get("key_p4"), projectId,
        paMemberSetRef});
    List<String> piProjectTokens = SafeUtils.getTokens(passDelegation("key_p4", projectToken,
      projectId));

    envs.clear();
    //Authorize that PI can create slice
    envs.put(subject, principalMap.get("key_p4"));
    //bearerRef should be subject set, as it contains both project token and MA token
    envs.put(bearerRef, piProjectTokens.get(1));
    assert authorize(createSlice, "key_p3", new String[]{projectId}, envs);

    //PI delegate to users

    for (String slice : clientSlices) {
      String userKeyFile = sliceKeyMap.get(slice);
      String userKey = principalMap.get(sliceKeyMap.get(slice));
      String pmToken = safePost(postProjectMembership, "key_p4", new String[]{userKey,
        projectId, "true"});
      List<String> tokens = SafeUtils.getTokens(passDelegation(sliceKeyMap.get(slice), pmToken,
        projectId));
      envs.clear();
      envs.put(subject, userKey);
      envs.put(bearerRef, tokens.get(1));
      assert authorize(createSlice, "key_p3", new String[]{projectId}, envs);
    }

    /*
    The previous part is the common geni trust structure.
    The next is specific to our example

     */
    //create slices.
    String sliceControlRef = safePost(postStandardSliceControlSet, "key_p3");
    String slicePrivRef = safePost(postStandardSliceDefaultPrivilegeSet, "key_p3");
    for (String slice : clientSlices) {
      String sliceId = principalMap.get("key_p3") + ":" + slice;
      sliceScid.put(slice, sliceId);
      sliceToken.put(slice, safePost(postSliceSet, "key_p3",
        new String[]{principalMap.get(sliceKeyMap.get(slice)), sliceId, projectId,
          sliceControlRef,
          slicePrivRef}));
       SafeUtils.getTokens(passDelegation(sliceKeyMap.get(slice),
       sliceToken.get(slice), sliceId));
    }

    //UserAcl
    for (String slice: clientSlices) {
      safePost(postUserAclEntry, "sdx",
        new String[]{principalMap.get(sliceKeyMap.get(slice))});
    }

    //post authorize policy
    safePost(postStitchPolicy, "sdx");

    //authorizeStitchByUid
    for (String slice: clientSlices) {
      authorize(authorizeStitchByUID, "sdx",
        new String[]{principalMap.get(sliceKeyMap.get(slice)), sliceScid.get(slice)});
    }

    //MakeIp Delegation
    safePost(postMakeIPTokenSet, "rpkiroot", new String[]{"192.1.1.1/24"});
    for (String slice: clientSlices) {
      String ip = sliceIpMap.get(slice);
      String user = sliceKeyMap.get(slice);
      String userKey = principalMap.get(sliceKeyMap.get(slice));
      String ipToken = safePost(postIPAllocate, "rpkiroot", new String[]{userKey, ip,
        "192.1.1.1/24"});
      safePost(postDlgToken, user, new String[]{ipToken, ip});
      assert authorize(authorizeOwnPrefix, "sdx", new String[]{userKey, ip});
    }

    //Tag delegation
    String tag = principalMap.get("tagauthority") + ":tag0";
    safePost(postTagSet, "tagauthority", new String[]{tag});
    for (String slice:clientSlices) {
      String user = sliceKeyMap.get(slice);
      String userKey = principalMap.get(sliceKeyMap.get(slice));
      String tagToken = safePost(postGrantTagPriv, "tagauthority", new String[]{userKey, tag});
      safePost(updateTagSet, user, new String[]{tagToken, tag});
    }

    //userTagAcl
    //user post Connect policy
    for (String slice: clientSlices) {
      String user = sliceKeyMap.get(slice);
      safePost(postUserTagAclEntry, user, new String[]{tag});
      safePost(postCustomerConnectionPolicy, user, new String[]{});
      safePost(postTagPrivilegePolicy, user, new String[]{});
      safePost(postCustomerPolicy, user, new String[]{});
    }


    //authZByUserAttr
    for (int i = 0; i < 2; i++) {
      String slice = clientSlices.get(i);
      String user = sliceKeyMap.get(slice);
      String userKey = principalMap.get(user);
      String ip = sliceIpMap.get(slice);
      for (int j = i + 1; j < 2; j++) {
        String peerSlice = clientSlices.get(j);
        String peer = sliceKeyMap.get(peerSlice);
        String peerKey = principalMap.get(peer);
        String peerIp = sliceIpMap.get(peerSlice);
        assert authorize(authZByUserAttr, "sdx", new String[]{userKey, ip, peerKey, peerIp});
      }
    }
    logger.debug("end");

  }

  private String getPrincipalId(String key){
    if(principalMap.containsKey(key)){
      return principalMap.get(key);
    }
    String pid =  SafeUtils.getPrincipalId(safeServer, key);
    if(pid!= null) {
      principalMap.put(key, pid);
    }
    return pid;
  }


  private void addPrincipals() {
    principals.add("sdx");
    principals.add("tagauthority");
    principals.add("geniroot");
    principals.add("rpkiroot");
    //MA
    principals.add("key_p1");
    //PA
    principals.add("key_p2");
    //SA
    principals.add("key_p3");

    for (int i = 4; i < 20; i++) {
      principals.add("key_p" + i);
    }
  }

  private void initPrincipals() {
    principals.forEach(p -> {
      initIdSetSubjectSet(p);
      principalMap.put(p, SafeUtils.getPrincipalId(safeServer, p));
    });
  }

  private void initIdSetSubjectSet(String key) {
    SafeUtils.postSafeStatements(safeServer, postIdSet, key, new String[]{key});
    String token = SafeUtils.getToken(SafeUtils.postSafeStatements(safeServer, postSubjectSet, key, new
      String[]{}));
    subjectSet.put(key, token);
  }

  private void checkAuthorization() {
    for (int i = 1; i < 6; i++) {
      assert authorize(authorizeStitchByUID, "sdx",
        new String[]{principalMap.get(sliceKeyMap.get(slices[i])), sliceScid.get(slices[i])});
    }
    for (int i = 1; i < 6; i++) {
      String slice = slices[i];
      String user = sliceKeyMap.get(slice);
      String userKey = principalMap.get(user);
      String ip = sliceIpMap.get(slice);
      for (int j = i + 1; j < 6; j++) {
        String peerSlice = slices[j];
        String peer = sliceKeyMap.get(peerSlice);
        String peerKey = principalMap.get(peer);
        String peerIp = sliceIpMap.get(peerSlice);
        authorize(authZByUserAttr, "sdx", new String[]{userKey, ip, peerKey, peerIp});
      }
    }
  }


  String safePost(String method, String principal) {
    return safePost(method, principal, new String[]{});
  }

  String safePost(String method, String principal, String[] others) {
    String p = principalMap.get(principal);
    return SafeUtils.getToken(
      SafeUtils.postSafeStatements(safeServer, method, p, others)
    );
  }

  boolean authorize(String method, String principal, String[] otherValues) {
    String p = principalMap.get(principal);
    return SafeUtils.authorize(safeServer, method, p, otherValues);
  }

  boolean authorize(String method, String principal, String[] otherValues, HashMap<String,
    String> envs) {
    String p = principalMap.get(principal);
    return SafeUtils.authorize(safeServer, method, p, otherValues, envs);
  }

  String simpleEndorseMent(String method, String from, String to) {
    String fp = principalMap.get(from);
    String tp = principalMap.get(to);
    return SafeUtils.getToken(SafeUtils.postSafeStatements(safeServer, method, fp, new
      String[]{tp}));
  }

  String simpleEndorseMent(String method, String from, String to, String scid) {
    String fp = principalMap.get(from);
    String tp = principalMap.get(to);
    String token = SafeUtils.getToken(SafeUtils.postSafeStatements(safeServer, method, fp, new
      String[]{tp}));
    return SafeUtils.getToken(passDelegation(to, token, scid));
  }

  String passDelegation(String principal, String Token, String scid) {
    String p = principalMap.get(principal);
    return SafeUtils.postSafeStatements(safeServer, passDelegation, p, new String[]{Token, scid});
  }

  String simpleDelegation(String method, String from, String to, String scid) {
    String token = simpleEndorseMent(method, from, to, scid);
    return passDelegation(to, token, scid);
  }
}
