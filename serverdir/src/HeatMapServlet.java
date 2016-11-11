package com.sussex.ase1.gpstry3;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.naming.*;
import java.sql.*;
import java.util.*;
import javax.sql.*;

public class HeatMapServlet extends HttpServlet
{
	/**
	 * Author:  Steve Dixon
	 * Date  :  09.11.2016 
	 */
	private static final long serialVersionUID = 3011125014614337060L;
	InitialContext 	ctx = null;
	DataSource 		ds = null;
	Connection 		conn = null;
	Statement 		stat = null;
	ResultSet 		rs = null;
	String			dbname = null;
	
	String 	maptype = "";
	String 	postcode = "";
	double 	latitudeMin = 0;
	double 	latitudeMax = 0;
	double 	longitudeMin = 0;
	double 	longitudeMax = 0;
	int		zoom = 10;

	String sql = "";
	String faction = "";
	
    public void init(ServletConfig config) throws ServletException
	{
    	super.init(config);
		try {
		      ctx = new InitialContext();
		      //  Get the name of the database and the form action from the init parameters in the web.xml file
		      dbname  = getServletConfig().getInitParameter("dbname");
		      faction = getServletConfig().getInitParameter("faction");

		      	//  setup a Datasource.
		      ds = (DataSource) ctx.lookup(dbname);
//		      ds = (DataSource) ctx.lookup("java:comp/env/jdbc/house_prices");

		      conn = ds.getConnection();
		      stat = conn.createStatement();
		    }
		    catch (SQLException se) {
		    	se.printStackTrace();
		    }
		    catch (NamingException ne) {
		    	ne.printStackTrace();
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
	    	
	    	
	    	//  get the maptype and postcode parameters entered in the url.
           	maptype  = req.getParameter("maptype");
        	postcode = req.getParameter("postcode");
        	
        	//  setup the default select statement for postcodes entered with a length greater than one character
        	sql = "SELECT postcode,latitude, longitude, price / 100000 as weight FROM price_by_postcode WHERE postcode like '" + postcode + "%'";       	

        	// set the correct zoom level based on the length of the postcode entered.
        	switch (postcode.length()) {
        	case 1:	zoom = 9;
        			// select statement will look for postcodes with one alphabetic character followed by a digit.
        			sql = "SELECT postcode,latitude, longitude, price / 100000 as weight FROM price_by_postcode WHERE postcode rlike '^" + postcode + "[0-9]'";       	
        			break;
        	case 2:	zoom = 10;
        			break;
        	case 3:	zoom = 12;
        	   		break;
        	default:
        			zoom = 15;
        			break;
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
        	out.println("        top: 8px;");      // 10px
        	out.println("        left: 25%;");
        	out.println("        z-index: 5;");
        	out.println("        background-color: #fff;");
        	out.println("        padding: 5px;");
        	out.println("        border: 1px solid #999;");
        	out.println("        text-align: center;");
        	out.println("        font-family: 'Roboto','sans-serif';");
        	out.println("        line-height: 26px;");   // 30px
        	out.println("        padding-left: 8px;");   // 10px
        	out.println("      }");
        	out.println("      #floating-panel {");
        	out.println("        background-color: #fff;");
        	out.println("        border: 1px solid #999;");
        	out.println("        left: 25%;");
        	out.println("        padding: 5px;");
        	out.println("        position: absolute;");
        	out.println("        top: 8px;");   //  10px
        	out.println("        z-index: 5;  ");
        	out.println("      }");
        	out.println("    </style>");
        	out.println("  </head>");

        	out.println("  <body>");
        	//					form newPostcode is posted back to the servlet with the new maptype and postcode selected 
        	out.println("		<form id='newPostcode' action='" + faction + "' method='POST' accept-charset='UTF-8'>");
        	out.println("			<input type='hidden' name='maptype' value='" + maptype + "'>");
        	out.println("			<input type='hidden' name='postcode' value='" + postcode + "'>");
        	out.println("		</form>");
        	out.println("    <div id='floating-panel'>");
        	out.println("	   <input type='text' id='pcode' size='10' maxlength='8' value='" + postcode + "'>");
        	out.println("      <button onclick='showMap()'>Post</button>");
        	out.println("    </div>");
        	out.println("    <div id='map'></div>");
        	out.println("    <script type='text/javascript'>");

        	      // This example requires the Visualization library. Include the libraries=visualization
        	      // parameter when you first load the API. For example:
        	      // <script src="https://maps.googleapis.com/maps/api/js?key=YOUR_API_KEY&libraries=visualization">

        	out.println("      	var map, heatmap;");
			out.println("		var	latitudeMin, latitudeMax, longitudeMin, longitudeMax;");
			
        	out.println("      function initMap() {");
        	out.println("        map = new google.maps.Map(document.getElementById('map'), {");
        	out.println("          zoom: " + zoom + ",");
        	out.println("          center: {lat: 51.509865, lng: -0.118092},");
        	out.println("          mapTypeId: 'satellite'");
        	out.println("        });");
        	out.println("        heatmap = new google.maps.visualization.HeatmapLayer({");
        	out.println("          data: getPoints(),");
        	out.println("          map: map");
        	out.println("        });");
         	out.println("		map.setCenter(new google.maps.LatLng((latitudeMin + latitudeMax)/2,(longitudeMin + longitudeMax)/2));");
         	out.println("   	map.setZoom(" + zoom + ");");
        	out.println("      }");
        	out.println("");
        	out.println("      function showMap() {");
        	out.println("           var len = document.getElementById('pcode').value");
        	out.println("           if (len.length>0){");
        	out.println("				var myForm = document.forms['newPostcode'];");
        	out.println("				myForm.elements['postcode'].value = document.getElementById('pcode').value");
        	out.println("				document.forms['newPostcode'].submit();");
        	out.println("           }else {");
        	out.println("               alert('Enter postcode');");
        	out.println("           }");
        	out.println("      }");

        	// getPoints function returns an array of latitude, longitude and price weight to the HeatmapLayer in the initMap function
        	int recs = printGetPoints(out);

            out.println("         latitudeMin=" + latitudeMin);
            out.println("         latitudeMax=" + latitudeMax);
            out.println("         longitudeMin=" + longitudeMin);
            out.println("         longitudeMax=" + longitudeMax);
            out.println("	    </script>");
            out.println("	    <script async defer");
            //																	Google Maps API key							visualization library must be set to use heatmap
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
	
	public int printGetPoints(PrintWriter out)
			throws ServletException, IOException
	{
		int cnt = 0;
    	// getPoints function returns an array of latitude, longitude and price weight to the HeatmapLayer in the initMap function
    	out.println("      function getPoints() {");
    	try{
        	rs = stat.executeQuery(sql);
        	if (!rs.next()){
        		System.out.println("13");
//        		out.println("<p>Postcode Not Found!</p>");
        	}
        	else {
        		rs.beforeFirst();
        		if (rs.next()) {
        			// Initially set the Minimum and Maximum longitude and latitude in servlet to the first location selected.
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

        			out.println("{location: new google.maps.LatLng(" + latitude + "," + longitude + "),weight: " + weight + "},");
        			cnt++;
        		}
                out.println("	        ];");
                out.println("latitudeMin=" + latitudeMin);
                out.println("latitudeMax=" + latitudeMax);
                out.println("longitudeMin=" + longitudeMin);
                out.println("longitudeMax=" + longitudeMax);

        	}
    	}
    	catch (SQLException e){
    		System.out.println("SQLException");
    		e.printStackTrace();
    	}
    	catch (NullPointerException e){
    		System.out.println("NullPointerExcveption");
    		e.printStackTrace();
    	}
        out.println("	      }");
        return cnt;
	}
}



