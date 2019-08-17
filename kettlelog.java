import Log.*;
import Item.*;
import java.io.*;
import java.sql.*;
import javafx.stage.*;
import javafx.scene.Scene;
import java.nio.file.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import javafx.collections.*;
import javafx.application.Application;

public class Kettlelog extends Application {
    @SuppressWarnings("unchecked")

    //================================================================================
    // INITIALIZATION
    //================================================================================
    public static int expanded = 0;
    public static int presscount = 0; 
    public static int filterSel = 0; //1:starred, 2:checked, 3:mostrecent, 4:none
    private static double w = 1024;
    private static double w_to_h = 1.4;
    private static double h = w / w_to_h;
    private static double spacefromtable = 7.5;
    private static double xBounds = 0.0;
    private static double yBounds = 0.0;
    private static double extraheight = 5.0;
    public static boolean starred = false;
    public static boolean duplicatefound = false;

    private static ObservableList<Item> data = FXCollections.observableArrayList();
    private static ObservableList<Log> emptylist = FXCollections.observableArrayList();
    private static ObservableList<Item> itemsToDelete;
    private static Item empty = new Item("emptyID", "", "", "", "", "", "", false, false, "", "", emptylist);

    public static PrimaryStage primaryStage = new PrimaryStage();
    private static InfoStage infoStage = new InfoStage();
    private static AddStage addStage = new AddStage();
    private static AlertStage alertStage = new AlertStage();
    private static InsertData app = new InsertData();
    private static NotifStage notifStage = new NotifStage();

    //================================================================================
    // METHODS
    //================================================================================
    public static void main(String[] args) {
        launch(args);
    }
 
    @Override
    public void start(Stage setup) {
        loadData();
        itemsToDelete = FXCollections.observableArrayList(empty);
        primaryStage.show();
        primaryStage.updatePrimaryStage(data);
    
    }

    public PrimaryStage getPrimaryStage(){
        return primaryStage;
    }

    public AddStage getAddStage(){
        return addStage;
    }

    public InfoStage getInfoStage(){
        return infoStage;
    }

    public void showAddStage(int popuptype, String[] textarray, Item rowinfo){

        System.out.println("about to show add stage...");

        primaryStage.showOpaqueLayer();
        addStage.updateAddStage(popuptype, textarray, rowinfo);

        System.out.println("if this line shows, updateaddstage worked.");

        addStage.setX(primaryStage.getX() + primaryStage.getWidth() / 2 - 300);
        addStage.setY((primaryStage.getY() + primaryStage.getHeight() / 2 - 352.94) + extraheight);

        addStage.show();
    }


    public void hideAddStage(){
        primaryStage.hideOpaqueLayer();
        addStage.hideOpaqueLayer();
        addStage.hide();
    }

    public void showInfoStage(Item rowinfo) {
        primaryStage.showOpaqueLayer();
        infoStage.updateInfoStage(rowinfo);

        infoStage.setX(primaryStage.getX() + primaryStage.getWidth() / 2 - 250);
        infoStage.setY((primaryStage.getY() + primaryStage.getHeight() / 2 - 350) + extraheight);

        infoStage.show();
    }

    public void hideInfoStage(){
        primaryStage.hideOpaqueLayer();
        infoStage.hide();
    } 

    public void showAlertStage(int popuptype, ObservableList<Item> itemsToDelete, Item rowinfo) {

        primaryStage.showOpaqueLayer();
        alertStage.updateAlertStage(popuptype, itemsToDelete, rowinfo);

        alertStage.setX(primaryStage.getX() + primaryStage.getWidth() / 2 - 250); //250 is width divided by 2
        alertStage.setY((primaryStage.getY() + primaryStage.getHeight() / 2 - 175) + extraheight); 

        alertStage.show();
    }

    public void hideAlertStage(int popuptype){
    	if (popuptype == 1) {
        	primaryStage.hideOpaqueLayer();
    	}
        alertStage.hide();
        itemsToDelete.clear();
    } 

    public void showNotifStage() {

        primaryStage.showOpaqueLayer();
        //notifStage.updateNotifStage();

        notifStage.show();
    }

    public void hideNotifStage() {

        primaryStage.hideOpaqueLayer();

        notifStage.hide();
    }

    public boolean isItemChecked(){
        for(int i = 0; i<data.size(); i++){
            if(data.get(i).getChecked()==true){
                return true;
            }
        }
        return false;
    }

    public ObservableList<Item> getCheckedItems(){
        for(int i=0; i<data.size(); i++){
            Item curItem = data.get(i);
            if(curItem.getChecked()==true){
                itemsToDelete.add(curItem);
            }
        }
        return itemsToDelete;
    }

    public void enableRemoveBtn(){
        primaryStage.enableRemoveBtn();
    }

    public void disableRemoveBtn(){
        primaryStage.disableRemoveBtn();

    }

    public ObservableList<Item> getData(){
        return data;
    }

    public ObservableList<Log> getLogs(String id){
        //loop through data
        for(int i = 0; i < data.size(); i++){
            //find which item has correct id, return that list of logs
            if(data.get(i).getID().equals(id)){
                System.out.println("Got item: "+ data.get(i).getName());
                return data.get(i).getLogData();
            }
        }
        return null;
    }

    //getting an item just based off of its id.
    public Item getItem(String id){
        for (int i = 0; i < data.size(); i++){
            //when we eventually find a match, return that item
            if(data.get(i).getID().equals(id)){
                return data.get(i);
            }
        }
        return null;
    }

    public void clearSearchBar(){
        primaryStage.clearSearchBar();
    }

    public void setData(ObservableList<Item> items, int changetype){
        //===CHANGETYPE PARAMETER===//
        //0 refers to ADDING data from addwindow
        //1 refers to UPDATING data editwindow
        //2 refers to DELETING data from alertstage
        //3 refers to FILTERING data 

         switch(changetype){
            case 0:
                data.remove(empty);
                //Our observablelist from the parameter only has one item in this case, so let's take it out. 
                Item added = items.get(0);

                //We then need to do FOUR things with this item. 

                //1. Create a database(.db) file for it. 
                String dbname = added.getID() + ".db";
                createDataBase(dbname);

                //2. Create two tables for this item's database: one for info, one for log. 
                createTables(dbname);

                //3. Then, we need to populate the database tables with the information. 
                // The zero is referring to the starred. When a brand new item is added, the item is obviously not starred so it will be 0. 
                // 0 -> Not Starred, 1 -> Starred
                app.insertinfo(added.getID(), added.getName(), added.getStatus(), added.getQuantity(), added.getMinimum(), added.getDelivery(), added.getDesc(), 0, added.getDateAdded());
                app.insertlogs(added.getID(), added.getDate(), added.getQuantity());

                //4.Add it our data's observablelist so that we can display it in the table. 
                data.add(added); 
                
                break;
            case 2:
                for(int i = 0; i<items.size(); i++){ //cycle thru itemsToDelete list and remove from data 
                    //remove database
                    if(!items.get(i).getID().equals("emptyID")){
                        deleteDB(items.get(i).getID());
                    }
                    data.remove(items.get(i));
                }

                disableRemoveBtn();
                for(int j = 0; j<data.size(); j++){ //search items for checked property
                    if((data.get(j)).getChecked()==true){
                        enableRemoveBtn();
                    }
                }
                //searchbar.clear();
                if(data.size()==0){
                    data.add(empty);
                }
                hideAlertStage(1);
                break;
            case 3:
                data = items;
                break;
            default:
                System.out.println("other option");
            }
            primaryStage.updatePrimaryStage(data);

    }

    public void setAllChecked(boolean value){
        for(int i = 0; i<data.size(); i++){
            Item curItem = data.get(i);
            curItem.setChecked(value);
        } 
    }

    //This is a method that takes in an item name and checks if there is a duplicate in the existing data.
    public boolean duplicatefound(String itemname) {
        String lowername = itemname.toLowerCase();
        int length = data.size();
        for (int i = 0; i < length; i++) {
            String lowercasedata = (data.get(i)).getName().toLowerCase();
            if (lowercasedata.equals(lowername)) {
                return true; 
            } 
        }
        return false;
    }

    //================================================================================
    // DATABASE[SQL]
    //================================================================================

    //takes in a filename and creates a new database with that filename in the db folder of Kettlelog.
    //filename needs to be UNIQUE in order to create the database. otherwise, we're just connecting to an existing database. 
    public static void createDataBase(String filename){

        String url = "jdbc:sqlite:./db/kettledb/" + filename;
        Connection conn = null;
 
        try {
            conn = DriverManager.getConnection(url);
            if (conn != null) {
                System.out.println("A new database has been created.");
            }
            conn.close();
 
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


    public static Connection getDataBase(String dbName){
        //initialize connection
        Connection conn = null;
        try {
            String url = "jdbc:sqlite:./db/kettledb/" + dbName;
            conn = DriverManager.getConnection(url);
            System.out.println("Connection to SQLite has been established.");
            
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return conn;
    }

    //whenever an item is created, we need to create two tables in its database. 
    //the parameter will specify which database we attach the tables to. 
    public static void createTables(String filename) {

        // SQL statement that creates the table representing the info of the item (name, shipping time, minimum, etc.)
        // Notes: Checked is left out because if the user closes the program with something checked we don't need to save that check.
        //        Starred is represented as an integer, although it is a boolean. 
        String infotableSQL = "CREATE TABLE IF NOT EXISTS info ("
            + "id text primary key,"
            + "name text not null,"
            + "status text not null,"
            + "quantity text not null,"
            + "minimumstock text not null,"
            + "deliverytime text not null,"
            + "description text not null,"
            + "starred int not null,"
            + "dateadded text not null"
            + ");";

        //Creating the log table in the same database. 
        String logtableSQL = "CREATE TABLE IF NOT EXISTS log ("
            + "logdate text primary key,"
            + "logquan text not null"
            + ");";

        try{
            Connection conn = getDataBase(filename);
            Statement stmt = conn.createStatement();
            //creating our two tables in the database
            stmt.execute(infotableSQL);
            stmt.execute(logtableSQL);
            System.out.println("Tables have been created successfully.");
            stmt.close();
            conn.close();
        } 
        catch (SQLException e) {
            System.out.println("tablecatch");
            System.out.println(e.getMessage());
        }

    }

    //This method will add a log to certain database's Log Table, specified by the Name parameter. 
    public static void addLog(String id, String date, String quantity) {
        app.insertlogs(id, date, quantity); 
    }

    //Method that edits the information table for a specific database. 
    public static void editInfoTable(String id, String name, String status, String quantity, String minimum, String delivery, String desc, int starbool, String dateadded) {
        app.editinfo(id, name, status, quantity, minimum, delivery, desc, starbool, dateadded);
    }

    //This method will delete a single log from the SQL Database for an item.
    public static void deleteLog(String itemid, String logid) {
        System.out.println("helloworlddelete");
        app.removeLog(itemid, logid);
    }

    public void updateStarredDB(String id, boolean value){

        //find file with that id
        Connection conn = getDataBase(id+".db");

        try{
            String cmd = "UPDATE info SET starred = " + value + " WHERE id = " + id +";";
            Statement stmt = conn.createStatement();
            stmt.execute(cmd);
            stmt.close();
            conn.close();
        }
        catch(SQLException e){
            System.out.println(e.getMessage());
        }
    }

    //id to find db, info to update to, type of info (logdate, logquan)
    public void updateLog(String id, String type, String info, String oldinfo){
        //get id from item
        //find file with that id
        Connection conn = getDataBase(id+".db");

        try{
            String cmd = "UPDATE log SET " + type + " = '" + info + "' WHERE logdate = '" + oldinfo +"';";
            System.out.println(cmd);
            Statement stmt = conn.createStatement();
            stmt.execute(cmd);
            stmt.close();
            conn.close();
        }
        catch(SQLException e){
            System.out.println(e.getMessage());
        }

    }

    public void deleteDB(String id){
        try {
            Files.delete(Paths.get("./db/kettledb/"+id+".db"));
        } catch (NoSuchFileException x) {
            System.err.format("%s: no such" + " file or directory%n", id+".db");
        } catch (DirectoryNotEmptyException x) {
            System.err.format("%s not empty%n", "kettledb");
        } catch (IOException x) {
            // File permission problems are caught here.
            System.err.println(x);
        }
    }

    //This method gets the quantity of the most recent date for an item.
    public String getNewQuan(String id, String mostrecentdate){
        Item rowinfo = getItem(id);
        ObservableList<Log> loglist = rowinfo.getLogData();
        for (int i = 0; i < loglist.size(); i++) {
            if (loglist.get(i).getDateLogged().equals(mostrecentdate)){
                return loglist.get(i).getQuanLogged();
            }
        }
        System.out.println("No log with that date found.");
        return null;        
    }

    //the purpose of this method is to take data from a .db database and load it into our code. 
    //two observablelists need to be created from the two tables in the database (ObservableList<Item>, ObservableList<Log>)

    public void loadData(){//ObservableList<Item> loadData(){
        //if .DS_Store exists, we're going to remove it completely. 
        File store = new File("./db/kettledb/.DS_Store");
        if (store.delete()) {
            System.out.println(".DS_Store has been deleted.");
        }

        File dir = new File("./db/kettledb");
        File[] dblist = dir.listFiles(); //store all databases in an array

        //if no files, exit function
        if (dblist.length==0){
            System.out.println("no files!");
            data.add(empty); //if there's no files, add empty
            return;
        }

        for (int i = 0; i < dblist.length; i++) { //loop through all dbs
            String dbName = dblist[i].getName();
            System.out.println("Loading data");
            System.out.println(dbName);

            if(dbName.equals(".gitignore")){
                System.out.println("not db file: skipped");
                i++;
                if(i==dblist.length && data.size()==0){
                    System.out.println("no files!");
                    data.add(empty);
                    return;
                }
                if(i<dblist.length){
                    dbName = dblist[i].getName();
                    System.out.println(dbName);
                }
                else{
                    return;
                }
            }

            try{
                //connect to the db
                Connection conn = getDataBase(dbName);
                Statement stmt = conn.createStatement();

                String getMainData = "SELECT * FROM info"; //Call one table "Data", 2nd table "Log"
                ResultSet mainData = stmt.executeQuery(getMainData);

                Item it = new Item();

	            while (mainData.next()) { //only one row in mainData
                    it.setID(mainData.getString("id"));
	                it.setName(mainData.getString("name"));
                    it.setStatus(mainData.getString("status"));
                    it.setQuantity(mainData.getString("quantity"));
                    it.setMinimum(mainData.getString("minimumstock"));
                    it.setDelivery(mainData.getString("deliverytime"));
                    it.setDesc(mainData.getString("description"));
                    it.setStarred(mainData.getInt("starred")==1); //1=1, true, 0=1, false
                    it.setDateAdded(mainData.getString("dateadded"));
                    it.setChecked(false);
                }

                String getLogData = "SELECT * FROM log";
                ResultSet logData = stmt.executeQuery(getLogData);

                ObservableList<Log> logInfo = FXCollections.observableArrayList();
                //get all info from log table, stick it in new Item
                while (logData.next()) {
                    Log itLog = new Log();
                    itLog.setDateLogged(logData.getString("logdate"));
                    itLog.setQuanLogged(logData.getString("logquan"));
                    logInfo.add(0, itLog);
                }

                //give main item a quantity from the last log
                /*String lastQuanLogged = logInfo.get(logInfo.size()-1).getQuanLogged();
                it.setQuantity(lastQuanLogged);*/

                String lastDateLogged = logInfo.get(logInfo.size()-1).getDateLogged();
                it.setDate(lastDateLogged);

                //add observable list to item
                it.setLogData(logInfo);

                //add item to data
                data.add(it);

                stmt.close();
                conn.close();
            }
            catch (SQLException e) {
                System.out.println(e.getMessage());
                System.out.println("not working.");
            }
        }
    }


}