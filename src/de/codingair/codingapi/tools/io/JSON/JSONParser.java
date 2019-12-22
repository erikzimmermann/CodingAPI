package de.codingair.codingapi.tools.io.JSON;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

public class JSONParser extends org.json.simple.parser.JSONParser {
    @Override
    public Object parse(String s) throws ParseException {
        Object o = super.parse(s.replace("\\7\"", "\\\\\\\\\\\\\\\""));

        if(o instanceof JSONObject) {
            return new JSON((JSONObject) o);
        } else return o;
    }
}
