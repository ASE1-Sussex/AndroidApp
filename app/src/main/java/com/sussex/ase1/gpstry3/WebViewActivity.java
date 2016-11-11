
package com.sussex.ase1.gpstry3;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;

public class WebViewActivity extends AppCompatActivity {

    private WebView webView;
    private Context context;
    private String postcode;
    private String typeFind;
    private String lat;
    private String lon;
    private String url;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview);

        Intent i = getIntent();
        if(i.hasExtra("typeFind"))
        {
            typeFind = i.getStringExtra("typeFind");
        }
        if(typeFind.equals("P")) {
            if (i.hasExtra("postcode")) {
                postcode = i.getStringExtra("postcode");
            }
            url = createUrl(postcode);//"http://lowcost-env.kdumcfjv2e.us-west-2.elasticbeanstalk.com/TestMapServlet?maptype=%22heatmap%22&typeFind=P&postcode=" + postcode;
        }
        else
        {
            if (i.hasExtra("latitude")) {
                lat = i.getStringExtra("latitude");
            }
            if (i.hasExtra("longitude")) {
                lon = i.getStringExtra("longitude");
            }
            url = createUrl(lat,lon);//"http://lowcost-env.kdumcfjv2e.us-west-2.elasticbeanstalk.com/TestMapServlet?maptype=%22heatmap%22&typeFind=L&latitude=" + lat + "&longitude=" + lon;
        }

        webView = (WebView) findViewById(R.id.webView1);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(url);

    }

    public String createUrl(String pc) {
        return "http://lowcost-env.kdumcfjv2e.us-west-2.elasticbeanstalk.com/TestMapServlet?maptype=%22heatmap%22&typeFind=P&postcode=" + pc;
    }
    public String createUrl(String lat, String lng) {
        return "http://lowcost-env.kdumcfjv2e.us-west-2.elasticbeanstalk.com/TestMapServlet?maptype=%22heatmap%22&typeFind=L&latitude="+lat+"&longitude="+lng;
    }

}
/*import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;*/

/**
 * Created by Steve Dixon on 06/11/2016.
 */

/*public class GetMapActivity extends AppCompatActivity {

    private TextView postcode;
    private Button mapButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_map);
        Log.i("GETMAPSJD", "Start of MAP");

        postcode = (TextView) findViewById(R.id.postcode);      //Seconds between GPS Location Updates
        mapButton  = (Button) findViewById(R.id.mapButton);          //button to update settings

        Log.i("SETTINGSSJD", "End of Settings");

    } //end onCreate

    public void onClick(View view)
    {
        String postc = postcode.getText().toString();
        String mapUrl = "http://lowcost-env.kdumcfjv2e.us-west-2.elasticbeanstalk.com/TestMapServlet?maptype=heatmap&postcode=BN3";
        Intent Getintent = new Intent(Intent.ACTION_VIEW, Uri.parse(mapUrl));
        startActivity(Getintent);
    }


}*/


