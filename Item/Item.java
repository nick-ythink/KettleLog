package Item;
import Log.*;
import javafx.collections.*;

public class Item {

        private String id = null;
        private String name= null;
        private String status = null;
        private String quantity= null;
        private String minimum = null;
        private String delivery = null;
        private String desc = null;
        private boolean starred = false;
        private String date = null;
        private String dateadded = null;
        private boolean checked = false;
        private ObservableList<Log> logdata = FXCollections.observableArrayList();
        private String adc = null;
        private String rop = null;
        private String rod = null;


        public Item(){
        }

        public Item(String id, String name, String status, String quantity, String minimum, 
            String delivery, String desc, boolean starred, boolean checked, String date, 
            String dateadded, ObservableList<Log> logdata, String adc, String rop, String rod) {

            this.id = id;
            this.name = name;
            this.status = status;
            this.quantity = quantity;
            this.minimum = minimum;
            this.delivery = delivery;
            this.desc = desc;
            this.starred = starred;
            this.checked = checked;
            this.date = date;
            this.dateadded = dateadded;
            this.logdata = logdata;
            this.adc = adc;
            this.rop = rop;
            this.rod = rod;
 
        }

        public String getID() {
            return id;
        }

        public void setID(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getQuantity() {
            return quantity;
        }

        public void setQuantity(String quantity) {
            this.quantity = quantity;
        }

        public String getMinimum() {
            return minimum;
        }

        public void setMinimum(String minimum) {
            this.minimum = minimum;
        }

        public String getDelivery() {
            return delivery;
        }

        public void setDelivery(String delivery) {
            this.delivery = delivery;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public boolean getStarred() {
            return starred;
        }

        public void setStarred(boolean starred) {
            this.starred = starred;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getDateAdded() {
            return dateadded;
        }

        public void setDateAdded(String dateadded) {
            this.dateadded = dateadded;
        }
        
        public boolean getChecked() {
            return checked;
        }

        public void setChecked(boolean checked) {
            this.checked = checked;
        }

        public ObservableList<Log> getLogData(){
            return logdata;
        }

        public void setLogData(ObservableList<Log> logdata){
            this.logdata = logdata;
        }

        public String getADC(){
            return adc;
        }

        public void setADC(String adc){
            this.adc = adc;
        }

        public String getROP(){
            return rop;
        }

        public void setROP(String rop){
            this.rop = rop;
        }

        public String getROD(){
            //return "2019-08-20";
            //return "2020-08-25";
            //return "2020-08-23";
            return rod;
        }

        public void setROD(String rod){
            this.rod = rod;
        }

}