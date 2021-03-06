package exoplex.client.exogeni;

import exoplex.common.slice.SafeSlice;
import exoplex.common.utils.Exec;
import exoplex.common.utils.ServerOptions;
import exoplex.sdx.core.SliceManager;
import org.apache.commons.cli.CommandLine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.renci.ahab.libndl.resources.request.ComputeNode;
import org.renci.ahab.libtransport.util.TransportException;

import java.util.ArrayList;

/**
 * @author geni-orca
 */
public class ExogeniClientSlice extends SliceManager {
  static private long bw = 1000000;
  final Logger logger = LogManager.getLogger(ExogeniClientSlice.class);
  private String mask = "/24";
  private String type;
  private String subnet;
  private String routerSite = "";

  public ExogeniClientSlice() {
  }


  public ExogeniClientSlice(String[] args) {

    logger.debug("SDX-Simple " + args[0]);

    CommandLine cmd = ServerOptions.parseCmd(args);
    String configFilePath = cmd.getOptionValue("config");

    initializeExoGENIContexts(configFilePath);

    type = conf.getString("config.type");
    if (cmd.hasOption('d')) {
      type = "delete";
    }
  }

  public static void main(String[] args)throws  Exception {
    ExogeniClientSlice cs = new ExogeniClientSlice(args);
    cs.run();
  }

  public void run() throws Exception {
    //Example usage:   ./target/appassembler/bin/SafeSdxExample  ~/.ssl/geni-pruth1.pem ~/.ssl/geni-pruth1.pem "https://geni.renci.org:11443/orca/xmlrpc" pruth.1 stitch
    //Example usage:   ./target/appassembler/bin/SafeSdxExample  ~/.ssl/geni-pruth1.pem ~/.ssl/geni-pruth1.pem "https://geni.renci.org:11443/orca/xmlrpc" name fournodes


    if (type.equals("client")) {
      routerSite = conf.getString("config.routersite");
      subnet = conf.getString("config.ipprefix");
      computeIP(subnet);
      logger.info("Client start");
      String customerName = sliceName;
      SafeSlice c1 = createCustomerSlice(customerName, 2, IPPrefix, curip, bw, true);
      try {
        c1.commitAndWait();
      } catch (Exception e) {
        e.printStackTrace();
        c1 = createCustomerSlice(customerName, 2, IPPrefix, curip, bw, true);
        c1.commitAndWait();
      }
      c1.refresh();
      if(safeEnabled&&plexusAndSafeInSlice){
        String safeIp = c1.getComputeNode("safe-server").getManagementIP();
        checkSafeServer(safeIp, riakIp);
      }
      //copyFile2Slice(c1, "/home/yaoyj11/project/exo-geni/SAFE_SDX/src/main/resources/scripts/configospffornewif.sh","~/configospffornewif.sh","~/.ssh/id_rsa");
      //copyFile2Slice(c1, "/home/yaoyj11/project/exo-geni/SAFE_SDX/src/main/resources/scripts/configospffornewif.sh","~/configospffornewif.sh","~/.ssh/id_rsa");
      //runCmdSlice(c1,"/bin/bash ~/ospfautoconfig.sh","~/.ssh/id_rsa");
      configFTPService(c1, "(CNode1)", "ftpuser", "ftp");
      configQuaggaRouting(c1);
      logger.info("Slice active now: " + sliceName);
      c1.printNetworkInfo();
      return;
    } else if (type.equals("delete")) {
      SafeSlice s2 = null;
      logger.info("deleting slice " + sliceName);
      s2 = SafeSlice.loadManifestFile(sliceName, pemLocation, keyLocation, controllerUrl);
      s2.delete();
    }
  }

  public void configQuaggaRouting(SafeSlice c1){
    c1.runCmdSlice("apt-get update; apt-get install -y quagga iperf", sshkey, "CNode\\d+",
      true);
    String Prefix = subnet.split("/")[0];
    String mip = c1.getComputeNode("CNode1").getManagementIP();
    Exec.sshExec("root", mip, "echo \"ip route 192.168.1.1/16 " + Prefix + "\" >>/etc/quagga/zebra.conf  ", sshkey);
    Exec.sshExec("root", mip, "sed -i -- 's/zebra=no/zebra=yes/g' /etc/quagga/daemons\n", sshkey);
    Exec.sshExec("root", mip, "/etc/init.d/quagga restart", sshkey);
  }

  public void run(String customerName, String ipPrefix, String site) throws Exception {
    //Example usage:   ./target/appassembler/bin/SafeSdxExample  ~/.ssl/geni-pruth1.pem ~/.ssl/geni-pruth1.pem "https://geni.renci.org:11443/orca/xmlrpc" pruth.1 stitch
    //Example usage:   ./target/appassembler/bin/SafeSdxExample  ~/.ssl/geni-pruth1.pem ~/.ssl/geni-pruth1.pem "https://geni.renci.org:11443/orca/xmlrpc" name fournodes
    if (type.equals("client")) {
      routerSite = site;
      subnet = ipPrefix;
      computeIP(subnet);
      logger.info("Client start");
      SafeSlice c1 = createCustomerSlice(customerName, 2, IPPrefix, curip, bw, true);
      c1.commitAndWait();
      c1.refresh();
      if(safeEnabled && plexusAndSafeInSlice){
        String safeIp = c1.getComputeNode("safe-server").getManagementIP();
        checkSafeServer(safeIp, riakIp);
      }
      //copyFile2Slice(c1, "/home/yaoyj11/project/exo-geni/SAFE_SDX/src/main/resources/scripts/configospffornewif.sh","~/configospffornewif.sh","~/.ssh/id_rsa");
      //copyFile2Slice(c1, "/home/yaoyj11/project/exo-geni/SAFE_SDX/src/main/resources/scripts/configospffornewif.sh","~/configospffornewif.sh","~/.ssh/id_rsa");
      //runCmdSlice(c1,"/bin/bash ~/ospfautoconfig.sh","~/.ssh/id_rsa");
      configFTPService(c1, "(CNode1)", "ftpuser", "ftp");
      configQuaggaRouting(c1);
      logger.info("Slice active now: " + customerName);
      c1.printNetworkInfo();
      return;
    } else if (type.equals("delete")) {
      SafeSlice s2 = null;
      logger.info("deleting slice " + sliceName);
      s2 = SafeSlice.loadManifestFile(sliceName, pemLocation, keyLocation, controllerUrl);
      s2.delete();
    }
  }

  public SafeSlice createCustomerSlice(String sliceName, int num, String prefix, int start, long bw, boolean network)
      throws TransportException {//=1, String subnet="")
    //Main Example Code

    SafeSlice s = SafeSlice.create(sliceName, pemLocation, keyLocation, controllerUrl, sctx);

    ArrayList<ComputeNode> nodelist = new ArrayList<ComputeNode>();
    for (int i = 0; i < num; i++) {
      ComputeNode node0 = s.addComputeNode(routerSite, "CNode" + String.valueOf(i));
      nodelist.add(node0);
    }
    if(network){
      for(int i=0; i < nodelist.size() - 1; i++){
        s.addLink("clink" + i,
          IPPrefix + (start +i) + ".1",
          IPPrefix + (start +i) + ".2",
          "255.255.255.0",
          nodelist.get(i).getName(),
          nodelist.get(i + 1).getName(),
          bw
          );
      }
    }
    if (safeEnabled) {
      if(plexusAndSafeInSlice) {
        s.addSafeServer(serverSite, riakIp);
      }
    }
    return s;
  }
}

