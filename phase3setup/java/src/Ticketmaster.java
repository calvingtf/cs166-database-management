/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;





/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */

public class Ticketmaster{
	//reference to physical database connection
	private Connection _connection = null;
	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	
	public Ticketmaster(String dbname, String dbport, String user, String passwd) throws SQLException {
		System.out.print("Connecting to database...");
		try{
			// constructs the connection URL
			String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
			System.out.println ("Connection URL: " + url + "\n");
			
			// obtain a physical connection
	        this._connection = DriverManager.getConnection(url, user, passwd);
	        System.out.println("Done");
		}catch(Exception e){
			System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
	        System.out.println("Make sure you started postgres on this machine");
	        System.exit(-1);
		}
	}
	
	/**
	 * Method to execute an update SQL statement.  Update SQL instructions
	 * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
	 * 
	 * @param sql the input SQL string
	 * @throws java.sql.SQLException when update failed
	 * */
	public void executeUpdate (String sql) throws SQLException { 
		// creates a statement object
		Statement stmt = this._connection.createStatement ();

		// issues the update instruction
		stmt.executeUpdate (sql);

		// close the instruction
	    stmt.close ();
	}//end executeUpdate

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and outputs the results to
	 * standard out.
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQueryAndPrintResult (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		/*
		 *  obtains the metadata object for the returned result set.  The metadata
		 *  contains row and column info.
		 */
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;
		
		//iterates through the result set and output them to standard out.
		boolean outputHeader = true;
		while (rs.next()){
			if(outputHeader){
				for(int i = 1; i <= numCol; i++){
					System.out.print(rsmd.getColumnName(i) + "\t");
			    }
			    System.out.println();
			    outputHeader = false;
			}
			for (int i=1; i<=numCol; ++i)
				System.out.print (rs.getString (i) + "\t");
			System.out.println ();
			++rowCount;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the results as
	 * a list of records. Each record in turn is a list of attribute values
	 * 
	 * @param query the input query string
	 * @return the query result as a list of records
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException { 
		//creates a statement object 
		Statement stmt = this._connection.createStatement (); 
		
		//issues the query instruction 
		ResultSet rs = stmt.executeQuery (query); 
	 
		/*
		 * obtains the metadata object for the returned result set.  The metadata 
		 * contains row and column info. 
		*/ 
		ResultSetMetaData rsmd = rs.getMetaData (); 
		int numCol = rsmd.getColumnCount (); 
		int rowCount = 0; 
	 
		//iterates through the result set and saves the data returned by the query. 
		boolean outputHeader = false;
		List<List<String>> result  = new ArrayList<List<String>>(); 
		while (rs.next()){
			List<String> record = new ArrayList<String>(); 
			for (int i=1; i<=numCol; ++i) 
				record.add(rs.getString (i)); 
			result.add(record); 
		}//end while 
		stmt.close (); 
		return result; 
	}//end executeQueryAndReturnResult
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the number of results
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQuery (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		int rowCount = 0;

		//iterates through the result set and count nuber of results.
		if(rs.next()){
			rowCount++;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to fetch the last value from sequence. This
	 * method issues the query to the DBMS and returns the current 
	 * value of sequence used for autogenerated keys
	 * 
	 * @param sequence name of the DB sequence
	 * @return current value of a sequence
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	
	public int getCurrSeqVal(String sequence) throws SQLException {
		Statement stmt = this._connection.createStatement ();
		
		ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
		if (rs.next()) return rs.getInt(1);
		return -1;
	}

	/**
	 * Method to close the physical connection if it is open.
	 */
	public void cleanup(){
		try{
			if (this._connection != null){
				this._connection.close ();
			}//end if
		}catch (SQLException e){
	         // ignored.
		}//end try
	}//end cleanup

	/**
	 * The main execution method
	 * 
	 * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
	 */
	public static void main (String[] args) {
		if (args.length != 3) {
			System.err.println (
				"Usage: " + "java [-classpath <classpath>] " + Ticketmaster.class.getName () +
		            " <dbname> <port> <user>");
			return;
		}//end if
		
		Ticketmaster esql = null;
		
		try{
			System.out.println("(1)");
			
			try {
				Class.forName("org.postgresql.Driver");
			}catch(Exception e){

				System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
				e.printStackTrace();
				return;
			}
			
			System.out.println("(2)");
			String dbname = args[0];
			String dbport = args[1];
			String user = args[2];
			
			esql = new Ticketmaster (dbname, dbport, user, "");
			
			boolean keepon = true;
			while(keepon){
				System.out.println("MAIN MENU");
				System.out.println("---------");
				System.out.println("1. Add User");
				System.out.println("2. Add Booking");
				System.out.println("3. Add Movie Showing for an Existing Theater");
				System.out.println("4. Cancel Pending Bookings");
				System.out.println("5. Change Seats Reserved for a Booking");
				System.out.println("6. Remove a Payment");
				System.out.println("7. Clear Cancelled Bookings");
				System.out.println("8. Remove Shows on a Given Date");
				System.out.println("9. List all Theaters in a Cinema Playing a Given Show");
				System.out.println("10. List all Shows that Start at a Given Time and Date");
				System.out.println("11. List Movie Titles Containing \"love\" Released After 2010");
				System.out.println("12. List the First Name, Last Name, and Email of Users with a Pending Booking");
				System.out.println("13. List the Title, Duration, Date, and Time of Shows Playing a Given Movie at a Given Cinema During a Date Range");
				System.out.println("14. List the Movie Title, Show Date & Start Time, Theater Name, and Cinema Seat Number for all Bookings of a Given User");
				System.out.println("15. EXIT");
				
				/*
				 * FOLLOW THE SPECIFICATION IN THE PROJECT DESCRIPTION
				 */
				switch (readChoice()){
					case 1: AddUser(esql); break;
					case 2: AddBooking(esql); break;
					case 3: AddMovieShowingToTheater(esql); break;
					case 4: CancelPendingBookings(esql); break;
					case 5: ChangeSeatsForBooking(esql); break;
					case 6: RemovePayment(esql); break;
					case 7: ClearCancelledBookings(esql); break;
					case 8: RemoveShowsOnDate(esql); break;
					case 9: ListTheatersPlayingShow(esql); break;
					case 10: ListShowsStartingOnTimeAndDate(esql); break;
					case 11: ListMovieTitlesContainingLoveReleasedAfter2010(esql); break;
					case 12: ListUsersWithPendingBooking(esql); break;
					case 13: ListMovieAndShowInfoAtCinemaInDateRange(esql); break;
					case 14: ListBookingInfoForUser(esql); break;
					case 15: keepon = false; break;
				}
			}
		}catch(Exception e){
			System.err.println (e.getMessage ());
		}finally{
			try{
				if(esql != null) {
					System.out.print("Disconnecting from database...");
					esql.cleanup ();
					System.out.println("Done\n\nBye !");
				}//end if				
			}catch(Exception e){
				// ignored.
			}
		}
	}

	public static int readChoice() {
		int input;
		// returns only if a correct value is given.
		do {
			System.out.print("Please make your choice: ");
			try { // read the integer, parse it and break.
				input = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}//end try
		}while (true);
		return input;
	}//end readChoice
	
	//refrenced this for this function https://stackoverflow.com/questions/20536566/creating-a-random-string-with-a-z-and-0-9-in-java
	public static String getSaltString() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 18) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;

    }
	
	public static void AddUser(Ticketmaster esql){//1     
		String email; 
		String fname;
		String lname;
		String phone;
		String pwd;
                String query;
		
        //first name		
        while (true)
		{
			System.out.print("Please enter first name: ");
			try
			{
				fname = in.readLine();
				if (fname.length() <= 0 || fname.length() > 64) 
				{
					throw new RuntimeException("Your input is invalid! Name can not be larger that 64");
				}
				break;
			}
			catch (Exception e)
			{
				System.out.println("Your input is invalid! Your exception is: " + e.getMessage());
				continue;
			}
		}
		
		
		
		//last name
		
		while (true)
		{
			System.out.print("Please last name: ");
			try
			{
				lname = in.readLine();
				if (lname.length() <= 0 || lname.length() > 64) 
				{
					throw new RuntimeException("Your input is invalid! last Name can not be larger that 64");
				}
				break;
			}
			catch (Exception e)
			{
				System.out.println("Your input is invalid! Your exception is: " + e.getMessage());
				continue;
			}
		}
		
		//email
		
		while (true)
		{
			System.out.print("Please email address: ");
			try
			{
				email = in.readLine();
				if (email.length() <= 0 || email.length() > 64) 
				{
					throw new RuntimeException("Your input is invalid! Name can not be larger that 64");
				}
				break;
			}
			catch (Exception e)
			{
				System.out.println("Your input is invalid! Your exception is: " + e.getMessage());
				continue;
			}
		}
		
		 //password
		
			while (true)
		{
			System.out.print("Please enter your password : ");
			try
			{
				pwd = in.readLine();
				pwd=getSaltString();
				if (pwd.length() <= 0 || pwd.length() > 256) 
				{
					throw new RuntimeException("Your input is invalid! Name can not be larger that 64");
				}
				break;
			}
			catch (Exception e)
			{
				System.out.println("Your input is invalid! Your exception is: " + e.getMessage());
				continue;
			}
		}
		
		//phoneNuumber
		while (true)
		{
			System.out.print("Please enter phone number ex 9314736096 :");
			try
			{
				phone = in.readLine();
				if (phone.length() <= 0 || phone.length() > 10) 
				{
					throw new RuntimeException("Your input is invalid!");
				}
				break;
			}
			catch (Exception e)
			{
				System.out.println("Your input is invalid! Your exception is: " + e.getMessage());
				continue;
			}
		}
		
		try
		{
			//System.out.println( email + "," + lname + "," + fname + "," + phone + "," + pwd );
			//System.out.println("testing");
			
			query = "INSERT INTO Users " + "VALUES ('" + email + "', '" + lname + "', '" + fname + "', " + phone + ", '" + pwd +"')";
			System.out.println(query);
			esql.executeUpdate(query);
	
			int test=esql.executeQuery("SELECT * FROM Users");
			System.out.println(test);

		

// insert the data
         //query="INSERT INTO Users " + "VALUES (email, fname, lname,phone , pwd)";
		 //esql.executeUpdate(query);
			
		}
		catch (Exception e)
		{
			System.err.println("Query failed: " + e.getMessage());
		}
		
			}
		
			
		
			
			
		
			
			
	
		

		
	
	public static void AddBooking(Ticketmaster esql){//2
		
		
		
	String bid = "";
        String status = "";
        String bdatetime = "";
        String seats = "";
        int sid ;
        String email = "";
        String[] queries = new String[3];
        String insert_query  = "";
        int number_rows_returned = 0;
        int errors = 0;
		

        
		
		//booking id
		while (true)
		{
			System.out.print("Please eneter booking id: ");
			try
			{
				bid = in.readLine();
				if (bid.length() <= 0 || bid.length() > 128) 
				{
					throw new RuntimeException("Your input is invalid!");
				}
				break;
			}
			catch (Exception e)
			{
				System.out.println("Your input is invalid! Your exception is: " + e.getMessage());
				continue;
			}
		}

      
		//status
		
		while (true)
		{
			System.out.print("Please eneter status: (Paid, Canceled, Pending: ");
			try
			{
				status = in.readLine();
				if (status.length() <= 0 || status.length() > 64) 
				{
					throw new RuntimeException("Your input is invalid!");
				}
				break;
			}
			catch (Exception e)
			{
				System.out.println("Your input is invalid! Your exception is: " + e.getMessage());
				continue;
			}
		}
		

        //bdate
		while (true)
		{
			System.out.print("Please enter date/ and time in mm/dd/yy hh:mm:ss AM/PM format: ");
			try
			{
				bdatetime = in.readLine();
				if (bdatetime.length() <= 0 || bdatetime.length() > 64) 
				{
					throw new RuntimeException("Your input is invalid!");
				}
				break;
			}
			catch (Exception e)
			{
				System.out.println("Your input is invalid! Your exception is: " + e.getMessage());
				continue;
			}
		}
		
		

        
		
		//seats
			while (true)
		{
			System.out.print("Please enter number of seats booked: ");
			try
			{
				seats = in.readLine();
				if (bdatetime.length() <= 0 || seats.length() > 64) 
				{
					throw new RuntimeException("Your input is invalid!");
				}
				break;
			}
			catch (Exception e)
			{
				System.out.println("Your input is invalid! Your exception is: " + e.getMessage());
				continue;
			}
		}
		
		
		

       
		//show id
		while (true)
		{
			System.out.print("Please eneter show id: ");
			try 
			{
				sid = Integer.parseInt(in.readLine());
				break;
			}
			catch (Exception e)
			{
				System.out.println("Your input is invalid! Your exception is: " + e.getMessage());
				continue;
			}
		}
        
		//email
		
					while (true)
		{
			System.out.print("Please enter email ");
			try
			{
				email = in.readLine();
				if (email.length() <= 0 || email.length() > 64) 
				{
					throw new RuntimeException("Your input is invalid!");
				}
				break;
			}
			catch (Exception e)
			{
				System.out.println("Your input is invalid! Your exception is: " + e.getMessage());
				continue;
			}
		}
		
		

        
        queries[0] = "SELECT * FROM Bookings WHERE bid = " + bid;
        queries[1] = "SELECT * FROM Shows WHERE sid = " + sid;
        queries[2] = "SELECT * FROM Users WHERE email = \'" + email + "\'";

        for(int i = 0; i < 3; ++i){
            try { //check if bid, sid, or email exists
                number_rows_returned = esql.executeQueryAndPrintResult(queries[i]);
            }catch (SQLException e) {
                System.out.println("We did an oopsie on our end. Please try again later.");
                return;
            }

            switch(i){
                case 0:{
                    if(number_rows_returned > 0){
                        System.out.println("Error: Booking id " + bid + " already exists!");
                        errors++;
                    }
                    break;
                }
                case 1:	{
                    if(number_rows_returned == 0){
                        System.out.println("Error: Show with sid " + sid + " does not exist!");
                        errors++;
                    }
                    break;
                }
                case 2:{
                    if(number_rows_returned == 0){
                        System.out.println("Error: User with email " + email + " does not exist!");
                        errors++;
                    }
                    break;
                }
            }
        }
     
        if(errors > 0){
            System.out.println("Please fix all errors and try again");
            return;
        }else{
            insert_query = "INSERT INTO Bookings (bid, status, bdatetime, seats, sid, email) VALUES (\'" + bid + "\', \'" + status + "\', \'" + bdatetime + "\', \'" + seats + "\', \'" + sid + "\', \'" + email +"\')";
            try {
                esql.executeUpdate(insert_query);
                System.out.println("Booking " + bid + " has been successfully added. Have a nice day :)");
            }catch (SQLException e) {
                System.out.println("We did an oopsie on our end. Please try again later.");
            }
        }
		
		
		
		
		
		
		
	}
	
	public static void AddMovieShowingToTheater(Ticketmaster esql){//3
		//long ass function 
		
		//play
		String tid;
		
		
		
		
		String query;
		String query2;
		String query3;
		String query4;
		int rows;
		
		//movies 
		String mvid;
		String title;
		String rdate;
		String country;
		String description;
		String duration;
		String lang;
		String genre;
		
		//shows
		
		String sid;
		String sdate;
		String sttime;
		String edtime;
		
		
		//tid
			  while (true)
		{
			System.out.print("Please enter theater id: ");
			try
			{
				tid = in.readLine();
				if (tid.length() <= 0 || tid.length() > 9) 
				{
					throw new RuntimeException("Your input is invalid! Please enter correct booking id");
				}
				break;
			}
			catch (Exception e)
			{
				System.out.println("Your input is invalid! Your exception is: " + e.getMessage());
				continue;
			}
		}
		        query = "SELECT * FROM Theaters WHERE tid = " + tid;
				
			try { 
            rows = esql.executeQueryAndPrintResult(query);
        }catch (Exception e) {
            System.out.println(" Please try again later.");
            return;
        }
		
		
		
		
		//movie information
		
		//mvid
		
			  while (true)
		{
			System.out.print("Please enter the movie id ");
			try
			{
				mvid = in.readLine();
				if (mvid.length() <= 0 || mvid.length() > 1000000) 
				{
					throw new RuntimeException("Your input is invalid!");
				}
				break;
			}
			catch (Exception e)
			{
				System.out.println("Your input is invalid! Your exception is: " + e.getMessage());
				continue;
			}
		}
		
		
		//title
		
			  while (true)
		{
			System.out.print("Please enter movie title ");
			try
			{
				title = in.readLine();
				if (title.length() <= 0 || title.length() > 128) 
				{
					throw new RuntimeException("Your input is invalid! ");
				}
				break;
			}
			catch (Exception e)
			{
				System.out.println("Your input is invalid! Your exception is: " + e.getMessage());
				continue;
			}
		}
		
		//rdate
 	  while (true)
		{
			System.out.print("Please enter the release date MM/DD/YYYY ");
			try
			{
				rdate = in.readLine();
				if (rdate.length() <= 0 || rdate.length() > 10) 
				{
					throw new RuntimeException("Your input is invalid! ");
				}
				break;
			}
			catch (Exception e)
			{
				System.out.println("Your input is invalid! Your exception is: " + e.getMessage());
				continue;
			}
		}
		
		//Country
		
			  while (true)
		{
			System.out.print("Please enter the country: ");
			try
			{
				country= in.readLine();
				if (country.length() <= 0 || country.length() > 5) 
				{
					throw new RuntimeException("Your input is invalid! ");
				}
				break;
			}
			catch (Exception e)
			{
				System.out.println("Your input is invalid! Your exception is: " + e.getMessage());
				continue;
			}
		}
		
		//description
		
			  while (true)
		{
			System.out.print("Please enter the description: ");
			try
			{
				description = in.readLine();
				if (description.length() <= 0 || description.length() > 128) 
				{
					throw new RuntimeException("Your input is invalid! ");
				}
				break;
			}
			catch (Exception e)
			{
				System.out.println("Your input is invalid! Your exception is: " + e.getMessage());
				continue;
			}
		}
		
		//duration
		
			  while (true)
		{
			System.out.print("Please enter the duration ");
			try
			{
				duration = in.readLine();
				if (duration.length() <= 0 || duration.length() > 5) 
				{
					throw new RuntimeException("Your input is invalid! ");
				}
				break;
			}
			catch (Exception e)
			{
				System.out.println("Your input is invalid! Your exception is: " + e.getMessage());
				continue;
			}
		}
		
		//language 
		
			  while (true)
		{
			System.out.print("Please enter language code first two letters, example en for English: ");
			try
			{
				lang = in.readLine();
				if (lang.length() <= 0 || lang.length() > 2) 
				{
					throw new RuntimeException("Your input is invalid! ");
				}
				break;
			}
			catch (Exception e)
			{
				System.out.println("Your input is invalid! Your exception is: " + e.getMessage());
				continue;
			}
		}
		
		//genere
		
			  while (true)
		{
			System.out.print("Please enter genre: ");
			try
			{
				genre = in.readLine();
				if (genre.length() <= 0 || genre.length() > 128) 
				{
					throw new RuntimeException("Your input is invalid! Please enter correct booking id");
				}
				break;
			}
			catch (Exception e)
			{
				System.out.println("Your input is invalid! Your exception is: " + e.getMessage());
				continue;
			}
		}

     query2 = "INSERT INTO Movies (mvid, title, rdate, country, description, duration, lang, genre) VALUES (\'" + mvid + "\', \'" + title + "\', \'" + rdate + "\', \'" + country + "\', \'" + description + "\', \'" + duration +"\', \'" + lang +"\', \'" + genre +"\')";
        try {
            esql.executeUpdate(query2);
            System.out.println("Movie " + mvid + " added");
        }catch (Exception e) {
            System.out.println(" Please try again later.");
        }

		
		//shows 
		
		//show id
			  while (true)
		{
			System.out.print("Please enter show id: ");
			try
			{
				sid = in.readLine();
				if (sid.length() <= 0 || sid.length() > 128) 
				{
					throw new RuntimeException("Your input is invalid! ");
				}
				break;
			}
			catch (Exception e)
			{
				System.out.println("Your input is invalid! Your exception is: " + e.getMessage());
				continue;
			}
		}
		
		//show date
		
			  while (true)
		{
			System.out.print("Please enter show date: ");
			try
			{
				sdate = in.readLine();
				if (sdate.length() <= 0 || sdate.length() > 128) 
				{
					throw new RuntimeException("Your input is invalid! ");
				}
				break;
			}
			catch (Exception e)
			{
				System.out.println("Your input is invalid! Your exception is: " + e.getMessage());
				continue;
			}
		}
		
		//start time
		
			  while (true)
		{
			System.out.print("Please enter start time HH:MM ");
			try
			{
				sttime= in.readLine();
				if (sttime.length() <= 0 || sttime.length() > 5) 
				{
					throw new RuntimeException("Your input is invalid! ");
				}
				break;
			}
			catch (Exception e)
			{
				System.out.println("Your input is invalid! Your exception is: " + e.getMessage());
				continue;
			}
		}
		
		
		//end time
			  while (true)
		{
			System.out.print("Please enter end time HH:MM : ");
			try
			{
				edtime = in.readLine();
				if (edtime.length() <= 0 || edtime.length() > 128) 
				{
					throw new RuntimeException("Your input is invalid! ");
				}
				break;
			}
			catch (Exception e)
			{
				System.out.println("Your input is invalid! Your exception is: " + e.getMessage());
				continue;
			}
		}
		
		query3 = "INSERT INTO Shows (sid, mvid, sdate, sttime, edtime) VALUES (\'" + sid + "\', \'" + mvid + "\', \'" + sdate + "\', \'" + sttime + "\', \'" + edtime + "\')";
        try {
            esql.executeUpdate(query3);
            System.out.println("Show " + sid + " has been successfully added. Have a nice day :)");
        }catch (Exception e) {
            System.out.println("Please try again later.");
        }
		
		
		 query4 = "INSERT INTO Plays (sid, tid) VALUES (\'" + sid + "\', \'" + tid + "\')";
        try {
            esql.executeUpdate(query4);
            System.out.println("Play with Show " + sid + " and Theater " + tid + " has been successfully added.Have a nice day :)");
        } catch (Exception e) {
            System.out.println(" Please try again later.");
        }
		
		
	}
	
	public static void CancelPendingBookings(Ticketmaster esql){//4
		
		String input;
		String query;
		
		
		
		while(true) {
			System.out.println("Cancel all bookings? (y/n)");
			try {
				input = in.readLine();
				break;
			}
			catch (Exception e) {
				System.out.println("Your input is invalid! Your exception is: " + e.getMessage());
				continue;
			}
		}
		try {
			if (input.equals("y")) {
				query = "UPDATE Bookings SET seats = " + 0 + "WHERE status = 'Pending'" + ";"; 
				esql.executeUpdate(query);				
				
				query = "UPDATE Bookings SET status = 'Canceled' WHERE status = 'Pending'" + ";"; 
				esql.executeUpdate(query);
				
				System.out.println("Successfully canceled all pending Bookings."); 
			}
			else if (!input.equals("n")) {
				throw new RuntimeException("Please input (y/n) next time");
			}
		}
		catch (Exception e) {
			System.out.println("Your input is invalid! Your exception is: " + e.getMessage());
		}
		
	}
	
	public static void ChangeSeatsForBooking(Ticketmaster esql) throws Exception{//5
		
			String bid;
			String sid;
			String sid2;
			List<List<String>> result = new ArrayList<List<String>>();
			String seats;
			String showSeats;
			String newSeat;
			String oldSeat;
		//bid	
			  while (true)
		{
			System.out.print("Please enter the booking id: ");
			try
			{
				bid = in.readLine();
				if (bid.length() <= 0 || bid.length() > 5) 
				{
					throw new RuntimeException("Your input is invalid! Please enter correct booking id");
				}
				break;
			}
			catch (Exception e)
			{
				System.out.println("Your input is invalid! Your exception is: " + e.getMessage());
				continue;
			}
		}
		
		// current seats
		        seats = "SELECT ssid FROM ShowSeats WHERE bid = " + bid;				
				try{
            esql.executeQueryAndPrintResult(seats);
        }catch (Exception e){
            System.out.println("Please try again later.");
            return;
        }


        // change seats 
		

	  
	  	  while (true)
		{
			System.out.print("Please select seats to change: ");
			try
			{
				sid = in.readLine();
				if (sid.length() <= 0 || sid.length() > 5) 
				{
					throw new RuntimeException("Your input is invalid! Please enter correct booking id");
				}
				break;
			}
			catch (Exception e)
			{
				System.out.println("Your input is invalid! Your exception is: " + e.getMessage());
				continue;
			}
		}
		
		// seats that match requirments 
		
		 showSeats = "SELECT ssid FROM ShowSeats WHERE bid IS NULL" +
                            " INTERSECT SELECT s1.ssid FROM ShowSeats s1 WHERE s1.price = (SELECT s2.price FROM ShowSeats s2 WHERE s2.ssid = " + sid + ")" +
                            " INTERSECT SELECT s1.ssid FROM ShowSeats s1, Plays p1 WHERE s1.sid = p1.sid AND p1.tid = (SELECT p2.tid FROM ShowSeats s2, Plays p2 WHERE s2.sid = p2.sid AND s2.ssid = " + sid + ")";
	  

		 try{
            result = esql.executeQueryAndReturnResult(showSeats);
        }catch (Exception e){
            System.out.println("Something went wrong sorry");
            return;
        }
		
		if(result.size()==0)
		{
		 System.out.println("Sorry no seats available at this time, please call customer support .");

		}else{
            System.out.print("Here are the seats that are still available at the same price: ");
            for(int i = 0; i < result.size(); ++i){
                System.out.print(result.get(i).get(0) + " ");
			}
		}
		
		//change seat
		
			  while (true)
		{
			System.out.print("Please add new seat ");
			try
			{
				sid2 = in.readLine();
				if (sid2.length() <= 0 || sid2.length() > 5) 
				{
					throw new RuntimeException("Your input is invalid! Please enter correct booking id");
				}
				break;
			}
			catch (Exception e)
			{
				System.out.println("Your input is invalid! Your exception is: " + e.getMessage());
				continue;
			}
		}
		
		
		
		
		       oldSeat = "UPDATE ShowSeats SET bid = NULL WHERE ssid = " + sid;
               newSeat = "UPDATE ShowSeats SET bid = " + bid + " WHERE ssid = " + sid2;
        try{
            esql.executeUpdate(oldSeat);
            esql.executeUpdate(newSeat);
            System.out.println("Booking has been successfully updated! :)");
        }catch (Exception e){
            System.out.println("Please try again later.");
            return;
        }
		
		

	}
	
	public static void RemovePayment(Ticketmaster esql){//6
	
	String pid;
	String bid;
	String query;
    List<List<String>> result = new ArrayList<List<String>>();
	
	String queryUpdate;
	String queryDelete;
	

		
		
		
		while(true) {
			System.out.println("Enter pid of user ");
			try {
				pid = in.readLine();
				break;
			}
			catch (Exception e) {
				System.out.println("Your input is invalid! Your exception is: " + e.getMessage());
				continue;
			}
		}
		
	
		
				query="SELECT bid FROM Payments WHERE pid = " + pid;

		
		try{
            result = esql.executeQueryAndReturnResult(query);
            bid = result.get(0).get(0);
            System.out.println("Booking " + pid + " found: " + bid);
		}catch (Exception e) {
			System.out.println("Your input is invalid! Your exception is: " + e.getMessage());
			return;
		}
		
		queryUpdate="UPDATE Bookings SET status = \'Cancelled\' WHERE bid = " + bid;
	    queryDelete = "DELETE FROM Payments WHERE pid = " + pid;
		
		try{
            esql.executeUpdate(queryUpdate);
            esql.executeUpdate(queryDelete);
			System.out.println("pid removed: " + pid);

		}catch (Exception e) {
				System.out.println("Your input is invalid! Your exception is: " + e.getMessage());
				return;
			}
			
			

		
	
		
	}
	
	public static void ClearCancelledBookings(Ticketmaster esql){//7
	
	   String query;
		List<List<String>> query_result  = new ArrayList<List<String>>();

		query = "SELECT COUNT (*) FROM bookings WHERE status = 'Canceled'";
		try {
			query_result = esql.executeQueryAndReturnResult(query);
		} catch(Exception e) {
			System.out.println(e.getMessage());
		}

		query = "DELETE FROM bookings WHERE status = 'Canceled'";
		try {
			esql.executeQuery(query);
		} catch(Exception e) {
		}
		System.out.println("\nTotal Bookings deleted: " + Integer.parseInt(query_result.get(0).get(0)));
		
		
	}
	
	public static void RemoveShowsOnDate(Ticketmaster esql){//8
		
		String date;
		
		String cid;
		
		String query;
		
		String deleteQuery;
		
		
		//date
		
		 while (true)
		{
			System.out.print("Please enter the date in MM/DD/YYYY: ");
			try
			{
				date = in.readLine();
				if (date.length() <= 0 || date.length() > 12) 
				{
					throw new RuntimeException("Your input is invalid! Please Enter valid date");
				}
				break;
			}
			catch (Exception e)
			{
				System.out.println("Your input is invalid! Your exception is: " + e.getMessage());
				continue;
		
	}
		}
		
		// cid
		 while (true)
		{
			System.out.print("Enter cid: ");
			try
			{
				cid = in.readLine();
				if (cid.length() <= 0 || cid.length() > 128) 
				{
					throw new RuntimeException("Your input is invalid! Please Enter valid date");
				}
				break;
			}
			catch (Exception e)
			{
				System.out.println("Your input is invalid! Your exception is: " + e.getMessage());
				continue;
		
	}
		}
		
		query = "SELECT * FROM Shows Where sdate = \'" + date + "\' AND sid IN (SELECT p.sid FROM Plays p, Theaters t WHERE p.tid = t.tid AND t.cid = " + cid + ")";
        System.out.println("Delete all Shows on the date " + date + " in Cinema " + cid + ": ");
		
		try{
			
            esql.executeQueryAndPrintResult(query);

	
		}		catch (Exception e)
			{
				System.out.println("Your input is invalid! Your exception is: " + e.getMessage());
				return;
		
	}
	
	
	        deleteQuery = "DELETE FROM Shows Where sdate = \'" + date + "\' AND sid IN (SELECT p.sid FROM Plays p, Theaters t WHERE p.tid = t.tid AND t.cid = " + cid + ")";
			
             try{
            esql.executeUpdate(deleteQuery);
            System.out.println("Deleted.");
        }catch (Exception e)
			{
				System.out.println("Your input is invalid! Your exception is: " + e.getMessage());
				return;
		
	}
	
	
	
	
	}
	
	public static void ListTheatersPlayingShow(Ticketmaster esql){//9
		//
		
		String cid;
		String sid;
		String query;
		
		
		// cid
		  while (true)
		{
			System.out.print("Please enter cid: ");
			try
			{
				cid = in.readLine();
				if (cid.length() <= 0 || cid.length() > 64) 
				{
					throw new RuntimeException("Your input is invalid! Name can not be larger that 64");
				}
				break;
			}
			catch (Exception e)
			{
				System.out.println("Your input is invalid! Your exception is: " + e.getMessage());
				continue;
			}
		}
		
		//sid
		
		  while (true)
		{
			System.out.print("Please enter sid: ");
			try
			{
				sid= in.readLine();
				if (sid.length() <= 0 || sid.length() > 64) 
				{
					throw new RuntimeException("Your input is invalid! Name can not be larger that 64");
				}
				break;
			}
			catch (Exception e)
			{
				System.out.println("Your input is invalid! Your exception is: " + e.getMessage());
				continue;
			}
		}
		
		    query = "SELECT t FROM Theaters t, Plays p WHERE p.sid = " + sid + " AND t.cid = " + cid + " AND p.tid = t.tid";
                    System.out.println("Here are all the  Theaters in Cinema " + cid + " playing the show " + sid + ": ");

		   try {
		esql.executeQueryAndPrintResult(query);

		} catch(Exception e) {
			System.out.println(e.getMessage());
		}
		
		
	}
	
	public static void ListShowsStartingOnTimeAndDate(Ticketmaster esql){//10
		//
		
		String date;
		String time;
		String query;
		
		// date
		 while (true)
		{
			System.out.print("Please enter the date in MM/DD/YYYY: ");
			try
			{
				date = in.readLine();
				if (date.length() <= 0 || date.length() > 12) 
				{
					throw new RuntimeException("Your input is invalid! Please Enter valid date");
				}
				break;
			}
			catch (Exception e)
			{
				System.out.println("Your input is invalid! Your exception is: " + e.getMessage());
				continue;
		
	}
		}
		
		
		//time
		
		 while (true)
		{
			System.out.print("Please enter the time in HM:MM example 1:00 or 0:56 : ");
			try
			{
				time = in.readLine();
				if (time.length() <= 0 || time.length() > 12) 
				{
					throw new RuntimeException("Your input is invalid! Please Enter valid date");
				}
				break;
			}
			catch (Exception e)
			{
				System.out.println("Your input is invalid! Your exception is: " + e.getMessage());
				continue;
		
	}
		}
		
		
		query = "SELECT * FROM Shows WHERE sdate = '" + date + "' AND sttime = '" + time + "'";
        System.out.println("All Shows that start on " + date + " at " + time + ": ");
		try {
			esql.executeQueryAndPrintResult(query);
		} catch(Exception e) {
			System.out.println(e.getMessage());
		}
		
		
		
	}

	public static void ListMovieTitlesContainingLoveReleasedAfter2010(Ticketmaster esql){//11
		//
		
		String query = "SELECT title FROM movies WHERE ";
		query += "(title like 'Love %'";
		query += "or title like 'love %'";
		query += "or title like '% Love'";
		query += "or title like '% love'";
		query += "or title like '% Love %'";
		query += "or title like '% love %') ";
		query += "and rdate >=  '2011-01-01' "; 
		query += "ORDER BY title";
		try {
			esql.executeQueryAndPrintResult(query);
		} catch(Exception e) {
			System.out.println(e.getMessage());
		}
		
	}

	public static void ListUsersWithPendingBooking(Ticketmaster esql){//12
		//
		String query = "SELECT u.fname, u.lname, u.email FROM Users u, Bookings b WHERE b.status = \'Pending\' AND b.email = u.email ";
		
        
		try {
			esql.executeQueryAndPrintResult(query);
		} catch(Exception e) {
			System.out.println(e.getMessage());
			return;
		}
		
	}

	public static void ListMovieAndShowInfoAtCinemaInDateRange(Ticketmaster esql){//13
		String startdate;
		String enddate;
		
		String cid;
		String mvid;
		
		String shows;
		
		List<List<String>> results = new ArrayList<List<String>>();

		
		
		//start day
		         while (true)
		{
			System.out.print("Enter start date as mm/dd/yy: ");
			try
			{
				startdate = in.readLine();
				if (startdate.length() <= 0 || startdate.length() > 10) 
				{
					throw new RuntimeException("Your input is invalid!");
				}
				break;
			}
			catch (Exception e)
			{
				System.out.println("Your input is invalid! Your exception is: " + e.getMessage());
				continue;
			}
		}
	
		
	// end date
		
		      while (true)
		{
			System.out.print("Enter end date as mm/dd/yy: ");
			try
			{
				enddate = in.readLine();
				if (startdate.length() <= 0 || enddate.length() > 10) 
				{
					throw new RuntimeException("Your input is invalid!");
				}
				break;
			}
			catch (Exception e)
			{
				System.out.println("Your input is invalid! Your exception is: " + e.getMessage());
				continue;
			}
		}
		//cid
		
		      while (true)
		{
			System.out.print("Enter enter cinema id ");
			try
			{
				cid = in.readLine();
				if (cid.length() <= 0 || cid.length() > 64) 
				{
					throw new RuntimeException("Your input is invalid!");
				}
				break;
			}
			catch (Exception e)
			{
				System.out.println("Your input is invalid! Your exception is: " + e.getMessage());
				continue;
			}
		}
		
		//mvid
		  while (true)
		{
			System.out.print("Enter enter movie id ");
			try
			{
				mvid= in.readLine();
				if (mvid.length() <= 0 || mvid.length() > 128) 
				{
					throw new RuntimeException("Your input is invalid!");
				}
				break;
			}
			catch (Exception e)
			{
				System.out.println("Your input is invalid! Your exception is: " + e.getMessage());
				continue;
			}
		}
		
		
		
		
		  
		  shows = "SELECT m.title as Title, m.duration  as Duration, s.sdate as Showdate, s.sttime FROM Plays p, Shows s, Cinemas c, Theaters t, Movies m WHERE c.cid = t.cid AND t.tid = p.tid AND p.sid = s.sid AND s.mvid = m.mvid " + 
            "AND m.mvid = " + mvid + " AND c.cid = " + cid + " AND s.sdate > \'" + startdate + "\' AND s.sdate < \'" + enddate + "\'";
        
		
		try { 
            results = esql.executeQueryAndReturnResult(shows);
        }catch (Exception e) {
            System.out.println(" Please try again later. " + e);
            return;
        }
		
		System.out.println("Displaying shows at Cinema " + cid + " with mvid " + mvid + " between " + startdate + " and " + enddate);
        for(List<String> dat: results){
                System.out.printf("|%30s %10s %15s %12s",
                dat.get(0) + " ", dat.get(1) + " ", dat.get(2) + " ", dat.get(3) + " |");
        }
       
    }
		
	

	
	
	
	
	
	public static void ListBookingInfoForUser(Ticketmaster esql){//14
		
		String email;
		int row;
		String query;
		String query2;
		
		 while (true)
		{
			System.out.print("Please enter email: ");
			try
			{
				email = in.readLine();
				if (email.length() <= 0 || email.length() > 128) 
				{
					throw new RuntimeException("Your input is invalid!");
				}
				break;
			}
			catch (Exception e)
			{
				System.out.println("Your input is invalid! Your exception is: " + e.getMessage());
				continue;
			}
		}
		
		
      query = "SELECT * FROM Bookings WHERE email = \'" + email + "\'";

      try { 
            row = esql.executeQueryAndPrintResult(query);
		
	}catch (Exception e)
			{
				System.out.println("Your input is invalid! Your exception is: " + e.getMessage());
				return;
			}
			
			if (row > 0){
            System.out.println("User " + email + " exists.");
        }
        else { 
            System.out.println("Error:" + email + " does not exist.");
            return;
        }
		
		query2 = "SELECT m.title, s.sdate, s.sttime, t.tname, cs.sno FROM Movies m, Shows s, Bookings b, ShowSeats ss, Theaters t, CinemaSeats cs WHERE b.email = \'" + email + "\' AND s.sid = b.sid AND m.mvid = s.mvid AND b.bid = ss.bid AND cs.csid = ss.csid AND cs.tid = t.tid";
       
        try{
            esql.executeQueryAndPrintResult(query2);
        }catch (Exception e)
			{
				System.out.println("Your input is invalid! Your exception is: " + e.getMessage());
				return;
			}
		
		
    } 
		
};
	



