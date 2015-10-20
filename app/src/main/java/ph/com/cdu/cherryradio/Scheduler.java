package ph.com.cdu.cherryradio;

public class Scheduler {
   
								/* START OF CLASS DECLARATION*/
   //SESSION CLASS
   private String fromTime;
   private String toTime;
   private String sessionDesc;
   private String songName;
   
   								/* END OF CLASS DECLARATION*/
   
   
   /* START OF SESSION METHOD*/
   
   public String getfromTime() {
       return fromTime;
   }

   public void setfromTime(String fromTime) {
       this.fromTime = fromTime;
   }
   
   public String gettoTime() {
       return toTime;
   }

   public void settoTime(String toTime) {
       this.toTime = " - " + toTime;
   }
   
   public String getsessionDesc() {
       return sessionDesc;
   }

   public void setsessionDesc(String sessionDesc) {
       this.sessionDesc = sessionDesc;
   }
   
   public String getsongName() {
       return sessionDesc;
   }

   public void setsongName(String songName) {
       this.songName = songName;
   }
   
}