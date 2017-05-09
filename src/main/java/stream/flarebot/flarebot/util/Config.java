package com.walshydev.jba;

import com.google.gson.*;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Config {

    private final Gson gson = new Gson();
    private final JsonParser parser = new JsonParser();

    private File configFile;
    private JsonObject object;

    public Config(String fileName){
        this.configFile = new File((fileName.endsWith(".json") ? fileName : fileName + ".json"));
        try {
            if(!configFile.exists())
                configFile.createNewFile();
            JsonElement element = new JsonParser().parse(new FileReader(configFile));
            if(element != null && !element.isJsonNull() && element.getAsJsonObject() != null)
                this.object = element.getAsJsonObject();
            else
                this.object = new JsonObject();
        } catch (IOException e) {
            JBA.LOGGER.error("There was an error creating the config file!", e);
        }
    }

    public JsonElement getElement(String s){
        if(this.object.has(s)){
            return this.object.get(s);
        }else{
            if(s.contains(".")){
                String[] split = s.split("\\.");
                JsonObject element = object.getAsJsonObject();
                for(String subPath : split){
                    if(element.has(subPath)){
                        if(element.get(subPath).isJsonObject() && !(split[split.length-1].equals(subPath))) {
                            element = element.get(subPath).getAsJsonObject();
                            continue;
                        }
                        return element.get(subPath);
                    }else{
                        throw new IllegalArgumentException("The specified member does not exist! Passed path '" + s + "', none-existent path: '" + subPath + "'");
                    }
                }
            }else{
                throw new IllegalArgumentException("The specified member does not exist! Passed path '" + s + "'");
            }
        }
        return null;
    }

    public JsonElement getElement(String path, JsonElement jsonElement){
        if(this.object.has(path)){
            return this.object.get(path);
        }else{
            if(path.contains(".")){
                String[] split = path.split("\\.");
                String lastSubPath = path.substring(path.lastIndexOf('.')+1);
                JsonObject element = object;
                for(int i = 0; i < split.length; i++){
                    String subPath = split[i];
                    if(element.has(subPath)){
                        if(element.get(subPath).isJsonObject() && !(lastSubPath.equals(subPath))) {
                            element = element.get(subPath).getAsJsonObject();
                            continue;
                        }
                        return element.get(subPath);
                    }else{
                        if(subPath.equals(lastSubPath)) {
                            element.add(subPath, jsonElement);
                            return element.get(subPath);
                        } else {
                            element.add(subPath, jsonElement);
                            if(element.get(subPath).isJsonObject())
                                element = element.get(subPath).getAsJsonObject();
                        }

                    }
                }
                return element;
            }else{
                object.add(path, jsonElement);
                return object.get(path);
                //throw new IllegalArgumentException("The specified member does not exist! Passed path '" + path + "'");
            }
        }
    }

    public String getString(String s){
        return this.getElement(s).getAsString();
    }

    public int getInt(String s){
        return this.getElement(s).getAsInt();
    }

    public List<String> getStringList(String s){
        List<String> list = new ArrayList<>();
        this.getElement(s).getAsJsonArray().forEach(element -> list.add(element.getAsString()));
        return list;
    }

    public JsonArray getArray(String s){
        return this.getElement(s).getAsJsonArray();
    }

    public JsonObject getObject(String s){
        return this.getElement(s).getAsJsonObject();
    }

    public boolean exists(String s) {
        try {
            return getElement(s) != null;
        }catch(IllegalArgumentException e){
            return false;
        }
    }

    /**
     * Set the JsonObject at the specified path to the Object passed (Serialized if needed).
     * @param path Path to the JsonObject
     * @param obj Object to set
     */
    public void set(String path, Object obj) {
        String lastSubPath = path;
        if(path.contains("."))
            lastSubPath = path.substring(0, path.lastIndexOf('.'));
        JsonElement element = getElement(lastSubPath, new JsonObject());

        if(element.isJsonObject())
            element.getAsJsonObject().add(lastSubPath, parser.parse(gson.toJson(obj)));
        else
            throw new IllegalArgumentException("Make sure the element at the specified path is a JsonObject not a JsonArray!!");
            //element.getAsJsonArray().add(parser.parse(gson.toJson(obj)));
    }

    /**
     * Add the Object passed (Serialized if needed) to the JsonObject at the specified path.
     * @param path Path to the JsonArray
     * @param obj Object to set
     */
    public void add(String path, Object obj){
        JsonElement element = getElement(path, new JsonArray());

        if(element.isJsonObject())
            throw new IllegalArgumentException("Make sure the element at the specified path is a JsonArray not a JsonObject!!");
        else
            element.getAsJsonArray().add(parser.parse(gson.toJson(obj)));
    }

    public void save(){
        try (FileWriter writer = new FileWriter(configFile)){
            writer.write(object.toString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public JsonObject getJsonObject() {
        return object;
    }

    public File getConfigFile() {
        return configFile;
    }
}
