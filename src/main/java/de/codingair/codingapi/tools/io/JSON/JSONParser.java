package de.codingair.codingapi.tools.io.JSON;


import de.codingair.codingapi.tools.io.lib.JSONObject;
import de.codingair.codingapi.tools.io.lib.ParseException;

public class JSONParser extends de.codingair.codingapi.tools.io.lib.JSONParser {
    @Override
    public Object parse(String s) throws ParseException {
        Object o = super.parse(s.replace("\\7\"", "\\\\\\\\\\\\\\\""));

        if(o instanceof JSONObject) {
            return new JSON((JSONObject) o);
        } else return o;
    }
}
