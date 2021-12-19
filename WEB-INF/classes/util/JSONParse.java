package util;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;
import java.util.*;
import java.io.StringWriter;
import java.io.IOException;


public class JSONParse {
  public String test_case = "{\"p2phash\":\"e49e1cbdb8cae0769828c23c7487e618e148dfd9daf0af948dae444b58d8a1d3\",\"sender\":\"1cbbf29e202076b9570a6f4ea8a63688056c879f7ddf51482370140b2f2a5662\",\"receiver\":\"1cbbf29e202076b9570a6f4ea8a63688056c879f7ddf51482370140b2f2a5662\",\"mhash\":\"11f64a1c4bf84e93672e970c6a4cbdd5b782f17cf17366bfe49d035531a13ae2\",\"msg\":\"163231000118034084227230169110232082110048071147041055091158050082155155031162117247219222071216253207023067081167000000\"}";
  private Object obj ;
  private JSONObject jobj ;
  private JSONParser parser = new JSONParser();

  /**
   * Parses the input json and returns a java object-map.
   * */
  public Map<String,Object> parse (String a){
    try{
      obj = parser.parse(a);
    }
    catch (ParseException e){
      System.out.println("Jason Parse failure!");
      System.out.println(a);
    }
    return (Map<String,Object>)obj;
  }

  /**
   * Deletes any existing obj, and creates a new one.
   * */
  public void create_obj () {
    jobj = new JSONObject();
  }

  /**
   * JSON to string
   * Takes a json object returns a json string
   * */
  public String jsts (JSONObject a){
    StringWriter out = new StringWriter();
    try{
      a.writeJSONString(out);
    }
    catch (IOException e) {
      System.out.println("Json to string fialure!");
      return "";
    }
    return out.toString();
  }

  /**
   * Returns a json string from the given input. 
   * @param recv    the receiver
   * @param status  boolean that turns to string "true"/"false"
   * @param val     value of the ack if it has one
   * */
  public String json_ack (String recv, Boolean status, String val){
    create_obj();
    jobj.put("receiver", recv);
    jobj.put("status", new Boolean(status));
    jobj.put("val", val);
    return jsts(jobj);
  }

  /**
   * Returns a json string from the given input. 
   * @param pph     p2p hash
   * @param sender  sender
   * @param recv    receiver
   * @param mh      msg hash
   * @param msg     msg
   * @param val     additional data
   * */
  public String json_msg (String pph, String sender, String recv, String mh, String msg, String val){
    create_obj();
    jobj.put("p2phash", pph);
    jobj.put("sender", sender);
    jobj.put("receiver", recv);
    jobj.put("mhash", mh);
    jobj.put("msg", msg);
    jobj.put("val", val);
    return jsts(jobj);
  }

  /**
   * Returns a json string from the given input. 
   * @param pph     p2p hash
   * @param sender  sender
   * @param recv    receiver
   * @param v1      data 
   * @param v2      data 
   * @param v3      data 
   * */
  public String json_request (String type, String sender, String recv, String v1, String v2, String v3){
    create_obj();
    jobj.put("type", type);
    jobj.put("sender", sender);
    jobj.put("receiver", recv);
    jobj.put("v1", v1);
    jobj.put("v2", v2);
    jobj.put("v3", v3);
    return jsts(jobj);
  }
  /**
   * Returns a json string from the given input. 
   * @param pph     p2p hash
   * @param sender  sender
   * @param recv    receiver
   * @param v1      data 
   * @param v2      data 
   * @param v3      data 
   * */
  public String json_request (String type, String sender, String recv, String v1, String v2, String v3, String v4){
    create_obj();
    jobj.put("type", type);
    jobj.put("sender", sender);
    jobj.put("receiver", recv);
    jobj.put("v1", v1);
    jobj.put("v2", v2);
    jobj.put("v3", v3);
    jobj.put("v4", v4);
    return jsts(jobj);
  }

}