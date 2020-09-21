package se.hig.dvg306.modul3app.tools;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ResourceHandler
{
    // used to read shader code
    public static String readTextData (Context context, int resourceId) throws IOException
    {
        StringBuffer dataBuffer = new StringBuffer ();
        String lineBuffer = null;

        InputStream resourceStream = context.getResources ().openRawResource (resourceId);
        InputStreamReader resourceStreamReader = new InputStreamReader (resourceStream);

        BufferedReader reader = new BufferedReader (resourceStreamReader);
        while ((lineBuffer = reader.readLine ()) != null)
        {
            dataBuffer.append (lineBuffer);
            dataBuffer.append ("\n");
        }
        reader.close();
        return dataBuffer.toString ();
    }

    // used to read model date
    public static List<String> readDataToList (Context context, int resourceId) throws IOException
    {
        String lineBuffer = null;

        InputStream resourceStream = context.getResources ().openRawResource (resourceId);
        InputStreamReader resourceStreamReader = new InputStreamReader (resourceStream);

        List<String> dataBuffer = new ArrayList<> ();

        BufferedReader reader = new BufferedReader (resourceStreamReader);
        while ((lineBuffer = reader.readLine ()) != null)
        {
            dataBuffer.add (lineBuffer);
        }

        reader.close();
        return dataBuffer;
    }
}
