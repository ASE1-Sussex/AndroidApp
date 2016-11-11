package heatmap;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.naming.*;
import java.sql.*;
import java.util.*;
import javax.sql.*;

public class TestMapServlet extends HttpServlet
{
	private static final long serialVersionUID = 3011125014614337060L;
	InitialContext 	ctx = null;
	DataSource 		ds = null;
	Connection 		conn = null;
	Statement 		stat = null;
	ResultSet 		rs = null;
	String			dbname = null;
	
	String 	maptype = "";
	String 	postcode = "";
	String 	typeFind = "";
	double latParam = 0.0;
	double lonParam = 0.0;
	double 	latitudeMin = 0;
	double 	latitudeMax = 0;
	double 	longitudeMin = 0;
	double 	longitudeMax = 0;
	int		maxZoom = 19;
	int 	minZoom = 6;
	int		zoom = 19;
	int 	radius = 1; 
	StringBuilder mapMarkers = null;
	String sql = "";
	String faction = "";
	
	// JDBC driver name and database URL
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
	static final String DB_URL = "jdbc:mysql://ase1sussex.ccfsoqbhse86.us-west-2.rds.amazonaws.com:3306/ase1";
	
	// Database credentials
	static final String USER = "user";
	static final String PASS = "password";
	
    public void init(ServletConfig config) throws ServletException
	{
    	super.init(config);
    	mapMarkers = new StringBuilder();
		try {
			
			//STEP 2: Register JDBC driver
		      Class.forName("com.mysql.jdbc.Driver");

		      //STEP 3: Open a connection
		      conn = DriverManager.getConnection(DB_URL,USER,PASS);
			
		      ctx = new InitialContext();
		      //  Get the name of the database and the form action from the init parameters in the web file
		      dbname  = "ase1";
		      faction = "http://lowcost-env.kdumcfjv2e.us-west-2.elasticbeanstalk.com/TestMapServlet";

		      	//  setup a Datasource.
		      ds = (DataSource) ctx.lookup(dbname);
//		      ds = (DataSource) ctx.lookup("java:comp/env/jdbc/house_prices");

		      conn = ds.getConnection();
		      stat = conn.createStatement();
		    }
		    catch (Exception se) {
		    	se.printStackTrace();
		    }
	}

	public void destroy()
	{
		// to do: code goes here.
	    try {
	        if (rs != null)
	          rs.close();
	        if (stat != null)
	          stat.close();
	        if (conn != null)
	          conn.close();
	        if (ctx != null)
	          ctx.close();
	      }   
	      catch (SQLException se) {
	    	  se.printStackTrace();
	      }
	      catch (NamingException ne) {
	    	  ne.printStackTrace();
	      }
	}

	public String getServletInfo()
	{
		return "Content page of menu system";
	}

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException
	{
		resp.setContentType("text/html");
	    	PrintWriter out = new PrintWriter(resp.getOutputStream());
	    	
	    	
	    	//  get the parameters entered in the url.
           	maptype  = req.getParameter("maptype");
           	typeFind = req.getParameter("typeFind");
           	
        	//  Setup the query based on the type request
           	if(typeFind.equals("P")) // P = Postcode
           	{
           		postcode = req.getParameter("postcode");
           		sql = "SELECT postcode,latitude, longitude, price/100000 as weight FROM price_by_postcode WHERE postcode like '" + postcode + "%'";
           		//sql = "SELECT  postcode, latitude, longitude, price as weight FROM price_by_postcode AS z JOIN (SELECT latitude as latpoint, longitude as longpoint, " + radius + " AS radius,      111.045 AS distance_unit FROM price_by_postcode WHERE postcode like '" + postcode + "%' limit 1) AS p ON 1=1 WHERE z.latitude BETWEEN p.latpoint  - (p.radius / p.distance_unit) AND p.latpoint  + (p.radius / p.distance_unit) AND z.longitude BETWEEN p.longpoint - (p.radius / (p.distance_unit * COS(RADIANS(p.latpoint)))) AND p.longpoint + (p.radius / (p.distance_unit * COS(RADIANS(p.latpoint))))";
           		
           	// set the correct zoom level based on the length of the postcode entered.
            	switch (postcode.length()) {
            	case 1:	zoom = 9;
            			// select statement will look for postcodes with one alphabetic character followed by a digit.
            			sql = "SELECT postcode,latitude, longitude, price/100000 as weight FROM price_by_postcode WHERE postcode rlike '^" + postcode + "[0-9]'";
            			//sql = "SELECT postcode,latitude, longitude, price as weight FROM price_by_postcode WHERE postcode like '" + postcode + "%'";
            			break;
            	case 2:	zoom = 10;
            			break;
            	case 3:	zoom = 12;
            	   		break;
            	case 4:	zoom = 15;
    	   				break;
            	case 5:	zoom = 17;
    	   				break;
            	default:
            			zoom = 19;
            			sql = "SELECT  postcode, latitude, longitude, price/100000 as weight FROM price_by_postcode AS z JOIN (SELECT latitude as latpoint, longitude as longpoint, " + radius + " AS radius,      111.045 AS distance_unit FROM price_by_postcode WHERE postcode like '" + postcode + "%' limit 1) AS p ON 1=1 WHERE z.latitude BETWEEN p.latpoint  - (p.radius / p.distance_unit) AND p.latpoint  + (p.radius / p.distance_unit) AND z.longitude BETWEEN p.longpoint - (p.radius / (p.distance_unit * COS(RADIANS(p.latpoint)))) AND p.longpoint + (p.radius / (p.distance_unit * COS(RADIANS(p.latpoint))))";
            			break;
            	}
           	}
           	else if(typeFind.equals("L")) // L = Location
           	{
           		latParam = Double.valueOf(req.getParameter("latitude"));
               	lonParam = Double.valueOf(req.getParameter("longitude"));
           		sql = "SELECT  postcode, latitude, longitude, price as weight FROM price_by_postcode AS z JOIN (SELECT  " + latParam + "  AS latpoint, " + lonParam + " AS longpoint," + radius + " AS radius,      111.045 AS distance_unit) AS p ON 1=1 WHERE z.latitude BETWEEN p.latpoint  - (p.radius / p.distance_unit) AND p.latpoint  + (p.radius / p.distance_unit) AND z.longitude BETWEEN p.longpoint - (p.radius / (p.distance_unit * COS(RADIANS(p.latpoint)))) AND p.longpoint + (p.radius / (p.distance_unit * COS(RADIANS(p.latpoint))))";
           	}

        	out.println("<!DOCTYPE html>");
        	out.println("<html>");
        	out.println("  <head>");
        	out.println("    <meta charset='utf-8'>");
        	out.println("    <title>Heat maps</title>");
        	out.println("    <style>");
        	      /* Always set the map height explicitly to define the size of the div
        	       * element that contains the map. */
        	out.println("      #map {");
        	out.println("        height: 100%;");
        	out.println("      }");
        	      /* Optional: Makes the sample page fill the window. */
        	out.println("      html, body {");
        	out.println("        height: 100%;");
        	out.println("        margin: 0;");
        	out.println("        padding: 0;");
        	out.println("      }");
        	//					floating panel contains the postcode entry field and selection button.
        	out.println("      #floating-panel {");
        	out.println("        position: absolute;");
        	out.println("        top: 10px;");
        	out.println("        right: 25%;");
        	out.println("        z-index: 5;");
        	out.println("        background-color: #fff;");
        	out.println("        padding: 5px;");
        	out.println("        border: 1px solid #999;");
        	out.println("        text-align: center;");
        	out.println("        font-family: 'Roboto','sans-serif';");
        	out.println("        line-height: 30px;");
        	out.println("        padding-left: 10px;");
        	out.println("      }");
        	out.println("      #floating-panel {");
        	out.println("        width:80px;");
        	out.println("        background-color: #fff;");
        	out.println("        border: 1px solid #999;");
        	out.println("        right: 25%;");
        	out.println("        padding: 5px;");
        	out.println("        position: absolute;");
        	out.println("        top: 10px;");
        	out.println("        z-index: 5;  ");
        	out.println("      }");
        	out.println("    </style>");
        	out.println("  </head>");
        	out.println("  <body>");
        	//					form newPostcode is posted back to the servlet with the new maptype and postcode selected 
        	out.println("		<form id='newPostcode' action='" + faction + "' method='POST' accept-charset='UTF-8'>");
        	out.println("			<input type='hidden' name='maptype' value='" + maptype + "'>");
        	out.println("			<input type='hidden' name='postcode' value='" + postcode + "'>");
        	out.println("			<input type='hidden' name='typeFind' value='P'>");
        	out.println("		</form>");
        	out.println("    <div id='floating-panel'>");
        	out.println("	 <input type='text' id='pcode' size='10' maxlength='8' value='" + postcode + "'>");
        	out.println("      <button onclick='showMap()'>Send</button>");
        	out.println("    </div>");
        	out.println("    <div id='map'></div>");
        	out.println("    <script type='text/javascript'>");

        	      // This example requires the Visualization library. Include the libraries=visualization
        	      // parameter when you first load the API. For example:
        	      // <script src="https://maps.googleapis.com/maps/api/js?key=YOUR_API_KEY&libraries=visualization">

        	out.println("      	var map, heatmap;");
        	out.println("      	var markerCount = 0;");
        	out.println("      	var markers = [];");
			out.println("		var	latitudeMin, latitudeMax, longitudeMin, longitudeMax;");
			
        	out.println("      function initMap() {");
        	out.println("        map = new google.maps.Map(document.getElementById('map'), {");
        	//out.println("          zoom: 5,");
        	out.println("          center: {lat: " + latParam + ", lng: " + lonParam + "},");
        	out.println("          mapTypeId: 'roadmap',");
        	out.println("          maxZoom: " + String.valueOf(maxZoom) + ",");
        	out.println("          minZoom: " + String.valueOf(minZoom) + ",");
        	out.println("        });");
        	out.println("        map.addListener('zoom_changed', function(){zoomChanged()});");
        	out.println("        heatmap = new google.maps.visualization.HeatmapLayer({");
        	out.println("          data: getPoints(),");
        	out.println("          radius: 250,");
        	//out.println("          dissipating: false,");
        	out.println("          opacity: 0.7,");
        	out.println("          gradient: [ 'rgba(35,173,14,0.0)',"); //35,173,14//0,15,112
        	out.println("          				'rgba(35,173,14,0.63)' ,");
        	out.println("          				'rgba(35,173,14,0.64)' ,");
        	out.println("          				'rgba(0,250,58,0.65)' ,");//0,250,58//0,171,250
        	out.println("          				'rgba(0,250,58,0.66)' ,");
        	out.println("          				'rgba(240,240,93,0.68)' ,");
        	out.println("          				'rgba(240,240,93,0.68)' ,");
        	out.println("          				'rgba(245,152,12,0.68)' ,");
        	out.println("          				'rgba(255,119,0,0.69)' ,");
        	out.println("          				'rgba(255,0,0,0.7)' ],");
        	out.println("          map: map");
        	out.println("        });");
         	out.println("		map.setCenter(new google.maps.LatLng((latitudeMin + latitudeMax)/2,(longitudeMin + longitudeMax)/2));");
         	if(typeFind.equals("L"))
         		out.println("   	map.setZoom(" + String.valueOf(maxZoom) + ");");
         	else
         	{
         		out.println("   	map.setZoom(" + String.valueOf(zoom) + ");");
         	}
         	out.println(mapMarkers.toString());
        	out.println("      }");
        	out.println("");
        	out.println("      function showMap() {");
        	out.println("			var myForm = document.forms['newPostcode'];");
        	out.println("			myForm.elements['postcode'].value = document.getElementById('pcode').value");
        	out.println("			document.forms['newPostcode'].submit();");
 //       	out.println("        	heatmap.setMap(heatmap.getMap() ? null : map);");
        	out.println("      }");

        	out.println("      function zoomChanged(){");
        	out.println("      		var currentZoom = this.map.getZoom()");
        	out.println("      		if (currentZoom >= 18 && currentZoom <= 19)");
        	out.println("      		{this.heatmap.radius = 100; clearMarkets(map); }");
        	out.println("      		else if (currentZoom >= 17 && currentZoom < 18)");
        	out.println("      		{this.heatmap.radius = 90;  clearMarkets(null);}");
        	out.println("      		else if (currentZoom >= 16 && currentZoom < 17)");
        	out.println("      		{this.heatmap.radius = 80;  clearMarkets(null);}");
        	out.println("      		else if (currentZoom >= 15 && currentZoom < 16)");
        	out.println("      		{this.heatmap.radius = 65;  clearMarkets(null);}");
        	out.println("      		else if (currentZoom >= 14 && currentZoom < 15)");
        	out.println("      		{this.heatmap.radius = 55;  clearMarkets(null);}");
        	out.println("      		else if (currentZoom >= 13 && currentZoom < 14)");
        	out.println("      		{this.heatmap.radius = 45;  clearMarkets(null);}");
        	out.println("      		else if (currentZoom >= 12 && currentZoom < 13)");
        	out.println("      		{this.heatmap.radius = 40;  clearMarkets(null);}");
        	out.println("      		else if (currentZoom >= 11 && currentZoom < 12)");
        	out.println("      		{this.heatmap.radius = 35;  clearMarkets(null);}");
        	out.println("      		else if (currentZoom >= 10 && currentZoom < 11)");
        	out.println("      		{this.heatmap.radius = 30;  clearMarkets(null);}");
        	out.println("      		else if (currentZoom >= 9 && currentZoom < 10)");
        	out.println("      		{this.heatmap.radius = 25;  clearMarkets(null);}");
        	out.println("      		else if (currentZoom >= 8 && currentZoom < 9)");
        	out.println("      		{this.heatmap.radius = 20;  clearMarkets(null);}");
        	out.println("      		else if (currentZoom >= 7 && currentZoom < 8)");
        	out.println("      		{this.heatmap.radius = 15;  clearMarkets(null);}");
        	out.println("      		else if (currentZoom >= 6 && currentZoom < 7)");
        	out.println("      		{this.heatmap.radius = 10;  clearMarkets(null);}");
        	out.println("      		else if (currentZoom >= 5 && currentZoom < 6)");
        	out.println("      		{this.heatmap.radius = 5;  clearMarkets(null);}");
        	out.println("      }");
        	
        	out.println("      function clearMarkets(map){");
        	out.println("      		for (var i = 0; i < markers.length; i++) {");
        	out.println("      		markers[i].setMap(map);}");
        	out.println("      }");
        	
        	out.println("      function addMarkerToMap(lat, lon, content){");
        	out.println("      var infowindow = new google.maps.InfoWindow();");
        	out.println("      var myLatLng = new google.maps.LatLng(lat, lon);");
        	out.println("      var marker = new google.maps.Marker({");
        	out.println("      		position: myLatLng,");
        	out.println("      		map: map,");
        	out.println("      		animation: google.maps.Animation.DROP,");
        	out.println("      });");
        	out.println("      markers.push(marker);");
        	out.println("      markerCount++;");
        	out.println("      google.maps.event.addListener(marker, 'click', (function(marker, markerCount) {");
        	out.println("      		return function() {");
        	out.println("      			infowindow.setContent(content);");
        	out.println("      			infowindow.open(map, marker);");
        	out.println("      		}");
        	out.println("      })(marker, markerCount));");
        	out.println("      map.panTo(myLatLng)}");
        	
        	// getPoints function returns an array of latitude, longitude and price weight to the HeatmapLayer in the initMap function
        	out.println("      function getPoints() {");
        	try{
        		Class.forName("com.mysql.jdbc.Driver");
        		conn = DriverManager.getConnection(DB_URL,USER,PASS);
        		stat = conn.createStatement();
            	rs = stat.executeQuery(sql);
            	if (!rs.next()){
            		System.out.println("13");
            	}
            	else {
            		rs.beforeFirst();
            		if (rs.next()) {
            			// Initially set the Minimum and Maximum longitude and latitude to the first location selected.
            			latitudeMin = rs.getDouble("latitude");
            			latitudeMax = latitudeMin;
            			longitudeMin = rs.getDouble("longitude");
            			longitudeMax = longitudeMin;
            		}
            		rs.beforeFirst();
            		out.println("        return [");
            		while(rs.next()) {
            			double lat = rs.getDouble("latitude");
            			double lon = rs.getDouble("longitude");
            			
            			// Find the Minimum and Maximum latitude and longitude selected. This will be used to calculate the centre of the map.
            			if (lat < latitudeMin) latitudeMin = lat;
            			if (lat > latitudeMax) latitudeMax = lat;
            			if (lon < longitudeMin) longitudeMin = lon;
            			if (lon > longitudeMax) longitudeMax = lon;
            			
            			String latitude = new Double(lat).toString();
            			String longitude = new Double(lon).toString();
            			String weight = new Double(rs.getDouble("weight")).toString();
            			if(!typeFind.equals("P") && zoom >= 17 && zoom <= 20)
            				mapMarkers.append("addMarkerToMap(" + latitude + ", " + longitude + ", '<p>" + weight + " &#163;</p>');\n");
            			out.println("{location: new google.maps.LatLng(" + latitude + "," + longitude + "),weight: " + weight + "},");
            		}
                    out.println("	        ];");
                    out.println("latitudeMin=" + latitudeMin);
                    out.println("latitudeMax=" + latitudeMax);
                    out.println("longitudeMin=" + longitudeMin);
                    out.println("longitudeMax=" + longitudeMax);

            	}
        	}
        	catch (Exception e){
        		System.out.println("SQLException");
        		e.printStackTrace();
        	}
            out.println("	      }");
            out.println("         latitudeMin=" + latitudeMin);
            out.println("         latitudeMax=" + latitudeMax);
            out.println("         longitudeMin=" + longitudeMin);
            out.println("         longitudeMax=" + longitudeMax);
            out.println("	    </script>");
            out.println("	    <script async defer");
            
            // google maps key
            out.println("	        src='https://maps.googleapis.com/maps/api/js?key=AIzaSyC-oMaxcF64dMrmSC39uN-Tk-CPfb9KvOc&libraries=visualization&callback=initMap'>");
            out.println("	    </script>");
            out.println("	  </body>");
            out.println("	  </html>");
    		out.flush();
    		out.close();
    	}   
		
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException
	{
		doGet(req, resp);
	}
}