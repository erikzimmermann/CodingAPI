package de.codingair.codingapi.tools.io.lib;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class JSONParser {
    public static final int S_INIT = 0;
    public static final int S_IN_FINISHED_VALUE = 1;
    public static final int S_IN_OBJECT = 2;
    public static final int S_IN_ARRAY = 3;
    public static final int S_PASSED_PAIR_KEY = 4;
    public static final int S_IN_PAIR_VALUE = 5;
    public static final int S_END = 6;
    public static final int S_IN_ERROR = -1;
    private LinkedList handlerStatusStack;
    private Yylex lexer = new Yylex((Reader)null);
    private Yytoken token = null;
    private int status = 0;

    public JSONParser() {
    }

    private int peekStatus(LinkedList statusStack) {
        if (statusStack.size() == 0) {
            return -1;
        } else {
            Integer status = (Integer)statusStack.getFirst();
            return status;
        }
    }

    public void reset() {
        this.token = null;
        this.status = 0;
        this.handlerStatusStack = null;
    }

    public void reset(Reader in) {
        this.lexer.yyreset(in);
        this.reset();
    }

    public int getPosition() {
        return this.lexer.getPosition();
    }

    public Object parse(String s) throws ParseException {
        return this.parse(s, (ContainerFactory)null);
    }

    public Object parse(String s, ContainerFactory containerFactory) throws ParseException {
        StringReader in = new StringReader(s);

        try {
            return this.parse((Reader)in, (ContainerFactory)containerFactory);
        } catch (IOException var5) {
            throw new ParseException(-1, 2, var5);
        }
    }

    public Object parse(Reader in) throws IOException, ParseException {
        return this.parse(in, (ContainerFactory)null);
    }

    public Object parse(Reader in, ContainerFactory containerFactory) throws IOException, ParseException {
        this.reset(in);
        LinkedList statusStack = new LinkedList();
        LinkedList valueStack = new LinkedList();

        try {
            do {
                this.nextToken();
                String key;
                Map parent;
                List newArray;
                label67:
                switch(this.status) {
                    case -1:
                        throw new ParseException(this.getPosition(), 1, this.token);
                    case 0:
                        switch(this.token.type) {
                            case 0:
                                this.status = 1;
                                statusStack.addFirst(new Integer(this.status));
                                valueStack.addFirst(this.token.value);
                                break label67;
                            case 1:
                                this.status = 2;
                                statusStack.addFirst(new Integer(this.status));
                                valueStack.addFirst(this.createObjectContainer(containerFactory));
                                break label67;
                            case 2:
                            default:
                                this.status = -1;
                                break label67;
                            case 3:
                                this.status = 3;
                                statusStack.addFirst(new Integer(this.status));
                                valueStack.addFirst(this.createArrayContainer(containerFactory));
                                break label67;
                        }
                    case 1:
                        if (this.token.type == -1) {
                            return valueStack.removeFirst();
                        }

                        throw new ParseException(this.getPosition(), 1, this.token);
                    case 2:
                        switch(this.token.type) {
                            case 0:
                                if (this.token.value instanceof String) {
                                    key = (String)this.token.value;
                                    valueStack.addFirst(key);
                                    this.status = 4;
                                    statusStack.addFirst(new Integer(this.status));
                                } else {
                                    this.status = -1;
                                }
                                break label67;
                            case 2:
                                if (valueStack.size() > 1) {
                                    statusStack.removeFirst();
                                    valueStack.removeFirst();
                                    this.status = this.peekStatus(statusStack);
                                } else {
                                    this.status = 1;
                                }
                            case 5:
                                break label67;
                            default:
                                this.status = -1;
                                break label67;
                        }
                    case 3:
                        List val;
                        switch(this.token.type) {
                            case 0:
                                val = (List)valueStack.getFirst();
                                val.add(this.token.value);
                                break label67;
                            case 1:
                                val = (List)valueStack.getFirst();
                                parent = this.createObjectContainer(containerFactory);
                                val.add(parent);
                                this.status = 2;
                                statusStack.addFirst(new Integer(this.status));
                                valueStack.addFirst(parent);
                                break label67;
                            case 2:
                            default:
                                this.status = -1;
                                break label67;
                            case 3:
                                val = (List)valueStack.getFirst();
                                newArray = this.createArrayContainer(containerFactory);
                                val.add(newArray);
                                this.status = 3;
                                statusStack.addFirst(new Integer(this.status));
                                valueStack.addFirst(newArray);
                                break label67;
                            case 4:
                                if (valueStack.size() > 1) {
                                    statusStack.removeFirst();
                                    valueStack.removeFirst();
                                    this.status = this.peekStatus(statusStack);
                                } else {
                                    this.status = 1;
                                }
                            case 5:
                                break label67;
                        }
                    case 4:
                        switch(this.token.type) {
                            case 0:
                                statusStack.removeFirst();
                                key = (String)valueStack.removeFirst();
                                parent = (Map)valueStack.getFirst();
                                parent.put(key, this.token.value);
                                this.status = this.peekStatus(statusStack);
                                break;
                            case 1:
                                statusStack.removeFirst();
                                key = (String)valueStack.removeFirst();
                                parent = (Map)valueStack.getFirst();
                                Map newObject = this.createObjectContainer(containerFactory);
                                parent.put(key, newObject);
                                this.status = 2;
                                statusStack.addFirst(new Integer(this.status));
                                valueStack.addFirst(newObject);
                                break;
                            case 2:
                            case 4:
                            case 5:
                            default:
                                this.status = -1;
                                break;
                            case 3:
                                statusStack.removeFirst();
                                key = (String)valueStack.removeFirst();
                                parent = (Map)valueStack.getFirst();
                                newArray = this.createArrayContainer(containerFactory);
                                parent.put(key, newArray);
                                this.status = 3;
                                statusStack.addFirst(new Integer(this.status));
                                valueStack.addFirst(newArray);
                            case 6:
                        }
                }

                if (this.status == -1) {
                    throw new ParseException(this.getPosition(), 1, this.token);
                }
            } while(this.token.type != -1);
        } catch (IOException var9) {
            throw var9;
        }

        throw new ParseException(this.getPosition(), 1, this.token);
    }

    private void nextToken() throws ParseException, IOException {
        this.token = this.lexer.yylex();
        if (this.token == null) {
            this.token = new Yytoken(-1, (Object)null);
        }

    }

    private Map createObjectContainer(ContainerFactory containerFactory) {
        if (containerFactory == null) {
            return new JSONObject();
        } else {
            Map m = containerFactory.createObjectContainer();
            return (Map)(m == null ? new JSONObject() : m);
        }
    }

    private List createArrayContainer(ContainerFactory containerFactory) {
        if (containerFactory == null) {
            return new JSONArray();
        } else {
            List l = containerFactory.creatArrayContainer();
            return (List)(l == null ? new JSONArray() : l);
        }
    }

    public void parse(String s, ContentHandler contentHandler) throws ParseException {
        this.parse(s, contentHandler, false);
    }

    public void parse(String s, ContentHandler contentHandler, boolean isResume) throws ParseException {
        StringReader in = new StringReader(s);

        try {
            this.parse((Reader)in, contentHandler, isResume);
        } catch (IOException var6) {
            throw new ParseException(-1, 2, var6);
        }
    }

    public void parse(Reader in, ContentHandler contentHandler) throws IOException, ParseException {
        this.parse(in, contentHandler, false);
    }

    public void parse(Reader in, ContentHandler contentHandler, boolean isResume) throws IOException, ParseException {
        if (!isResume) {
            this.reset(in);
            this.handlerStatusStack = new LinkedList();
        } else if (this.handlerStatusStack == null) {
            isResume = false;
            this.reset(in);
            this.handlerStatusStack = new LinkedList();
        }

        LinkedList statusStack = this.handlerStatusStack;

        try {
            do {
                label175:
                switch(this.status) {
                    case -1:
                        throw new ParseException(this.getPosition(), 1, this.token);
                    case 0:
                        contentHandler.startJSON();
                        this.nextToken();
                        switch(this.token.type) {
                            case 0:
                                this.status = 1;
                                statusStack.addFirst(new Integer(this.status));
                                if (!contentHandler.primitive(this.token.value)) {
                                    return;
                                }
                                break label175;
                            case 1:
                                this.status = 2;
                                statusStack.addFirst(new Integer(this.status));
                                if (!contentHandler.startObject()) {
                                    return;
                                }
                                break label175;
                            case 2:
                            default:
                                this.status = -1;
                                break label175;
                            case 3:
                                this.status = 3;
                                statusStack.addFirst(new Integer(this.status));
                                if (!contentHandler.startArray()) {
                                    return;
                                }
                                break label175;
                        }
                    case 1:
                        this.nextToken();
                        if (this.token.type == -1) {
                            contentHandler.endJSON();
                            this.status = 6;
                            return;
                        }

                        this.status = -1;
                        throw new ParseException(this.getPosition(), 1, this.token);
                    case 2:
                        this.nextToken();
                        switch(this.token.type) {
                            case 0:
                                if (this.token.value instanceof String) {
                                    String key = (String)this.token.value;
                                    this.status = 4;
                                    statusStack.addFirst(new Integer(this.status));
                                    if (!contentHandler.startObjectEntry(key)) {
                                        return;
                                    }
                                } else {
                                    this.status = -1;
                                }
                                break label175;
                            case 2:
                                if (statusStack.size() > 1) {
                                    statusStack.removeFirst();
                                    this.status = this.peekStatus(statusStack);
                                } else {
                                    this.status = 1;
                                }

                                if (!contentHandler.endObject()) {
                                    return;
                                }
                            case 5:
                                break label175;
                            default:
                                this.status = -1;
                                break label175;
                        }
                    case 3:
                        this.nextToken();
                        switch(this.token.type) {
                            case 0:
                                if (!contentHandler.primitive(this.token.value)) {
                                    return;
                                }
                                break label175;
                            case 1:
                                this.status = 2;
                                statusStack.addFirst(new Integer(this.status));
                                if (!contentHandler.startObject()) {
                                    return;
                                }
                                break label175;
                            case 2:
                            default:
                                this.status = -1;
                                break label175;
                            case 3:
                                this.status = 3;
                                statusStack.addFirst(new Integer(this.status));
                                if (!contentHandler.startArray()) {
                                    return;
                                }
                                break label175;
                            case 4:
                                if (statusStack.size() > 1) {
                                    statusStack.removeFirst();
                                    this.status = this.peekStatus(statusStack);
                                } else {
                                    this.status = 1;
                                }

                                if (!contentHandler.endArray()) {
                                    return;
                                }
                            case 5:
                                break label175;
                        }
                    case 4:
                        this.nextToken();
                        switch(this.token.type) {
                            case 0:
                                statusStack.removeFirst();
                                this.status = this.peekStatus(statusStack);
                                if (!contentHandler.primitive(this.token.value)) {
                                    return;
                                }

                                if (!contentHandler.endObjectEntry()) {
                                    return;
                                }
                                break label175;
                            case 1:
                                statusStack.removeFirst();
                                statusStack.addFirst(new Integer(5));
                                this.status = 2;
                                statusStack.addFirst(new Integer(this.status));
                                if (!contentHandler.startObject()) {
                                    return;
                                }
                                break label175;
                            case 2:
                            case 4:
                            case 5:
                            default:
                                this.status = -1;
                                break label175;
                            case 3:
                                statusStack.removeFirst();
                                statusStack.addFirst(new Integer(5));
                                this.status = 3;
                                statusStack.addFirst(new Integer(this.status));
                                if (!contentHandler.startArray()) {
                                    return;
                                }
                            case 6:
                                break label175;
                        }
                    case 5:
                        statusStack.removeFirst();
                        this.status = this.peekStatus(statusStack);
                        if (!contentHandler.endObjectEntry()) {
                            return;
                        }
                        break;
                    case 6:
                        return;
                }

                if (this.status == -1) {
                    throw new ParseException(this.getPosition(), 1, this.token);
                }
            } while(this.token.type != -1);
        } catch (IOException var6) {
            this.status = -1;
            throw var6;
        } catch (ParseException var7) {
            this.status = -1;
            throw var7;
        } catch (RuntimeException var8) {
            this.status = -1;
            throw var8;
        } catch (Error var9) {
            this.status = -1;
            throw var9;
        }

        this.status = -1;
        throw new ParseException(this.getPosition(), 1, this.token);
    }
}
