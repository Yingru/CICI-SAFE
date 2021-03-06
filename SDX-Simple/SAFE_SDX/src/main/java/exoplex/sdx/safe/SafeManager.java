package exoplex.sdx.safe;

import exoplex.common.slice.Scripts;
import exoplex.common.utils.Exec;
import exoplex.common.utils.SafeUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SafeManager {
  final static Logger logger = LogManager.getLogger(SafeManager.class);
  private String safeServerIp;
  private String safeServer;
  private String safeKeyFile;
  private String sshKey=null;
  private String safeKeyHash= null;

  public SafeManager(String ip, String safeKeyFile, String sshKey){
    safeServerIp = ip;
    safeServer = safeServerIp + ":7777";
    this.safeKeyFile = safeKeyFile;
    this.sshKey = sshKey;
  }

  public String getSafeKeyHash(){
    if(safeKeyHash == null){
      safeKeyHash = SafeUtils.getPrincipalId(safeServer, safeKeyFile);
    }
    return safeKeyHash;
  }

  public boolean authorizePrefix(String cushash, String cusip){
    String[] othervalues=new String[2];
    othervalues[0]=cushash;
    othervalues[1]=cusip;
    String message= SafeUtils.postSafeStatements(safeServer,"ownPrefix",
      getSafeKeyHash(),
      othervalues);
    if(message !=null && message.contains("Unsatisfied")){
      return false;
    }
    else
      return true;
  }


  public boolean authorizeConnectivity(String srchash, String srcip, String dsthash, String dstip){
    String[] othervalues=new String[4];
    othervalues[0]=srchash;
    othervalues[1]=srcip;
    othervalues[2]=dsthash;
    othervalues[3]=dstip;
    return SafeUtils.authorize(safeServer, "authZByUserAttr", getSafeKeyHash(),
      othervalues);
  }

  public boolean authorizeStitchRequest(String customer_slice,
                                        String customerName,
                                        String ReservID,
                                        String keyhash,
                                        String slicename,
                                        String nodename
  ){
    /** Post to remote safesets using apache httpclient */
    String[] othervalues=new String[5];
    othervalues[0]=customer_slice;
    othervalues[1]=customerName;
    othervalues[2]=ReservID;
    othervalues[3]=slicename;
    othervalues[4]=nodename;
    String message= SafeUtils.postSafeStatements(safeServer,"verifyStitch",
      getSafeKeyHash(),
      othervalues);
    if(message ==null || message.contains("Unsatisfied")){
      return false;
    }
    else
      return true;
  }

  public boolean authorizeStitchRequest(String customerSafeKeyHash,
                                        String customerSlice
  ){
    /** Post to remote safesets using apache httpclient */
    String[] othervalues=new String[2];
    othervalues[0]=customerSafeKeyHash;
    String saHash = SafeUtils.getPrincipalId(safeServer, "key_p3");
    String sdxHash = SafeUtils.getPrincipalId(safeServer, "sdx");
    othervalues[1]=saHash + ":" + customerSlice;
    return SafeUtils.authorize(safeServer, "authorizeStitchByUID", sdxHash, othervalues);
  }

  public void restartSafeServer(){
    Exec.sshExec("root", safeServerIp, Scripts.restartSafe_v1(),sshKey);
  }

  public void deploySafeScripts(){

  }

  public boolean verifySafeInstallation(String riakIp){
    if(safeServerAlive()){
      return true;
    }
    while(true) {
      String result = Exec.sshExec("root", safeServerIp, "docker images", sshKey)[0];
      if(result.contains("safeserver")){
        break;
      }else{
        Exec.sshExec("root", safeServerIp, Scripts.getSafeScript_v1(riakIp), sshKey);
      }
    }
    while(true){
      String result = Exec.sshExec("root", safeServerIp, "docker ps", sshKey)[0];
      if(result.contains("safe")){
        break;
      }else{
        Exec.sshExec("root", safeServerIp, Scripts.getSafeScript_v1(riakIp), sshKey);
      }
    }
    Exec.sshExec("root", safeServerIp, Scripts.restartSafe_v1(), sshKey);
    while (true){
      if(safeServerAlive()){
        break;
      }else{
        try{
          Thread.sleep(10000);
        }catch (Exception e){
        }
      }
    }
    return true;
  }

  private boolean safeServerAlive(){
    try{
      SafeUtils.getPrincipalId(safeServer, "sdx");
    }catch (Exception e){
      logger.debug("Safe server not alive yet");
      return false;
    }
    return true;
  }
}
