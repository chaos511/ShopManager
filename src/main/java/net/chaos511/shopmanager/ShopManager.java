package net.chaos511.shopmanager;


import com.google.gson.*;
import net.fabricmc.api.ModInitializer;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.MinecraftClient;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ShopManager implements ModInitializer {
    protected static JsonObject serversJson =new JsonObject();
    protected static String savedIp;
    protected static String savedShopName;
    protected static JsonObject savedshop;
    public static JsonObject savedServer;
    public static int signType;
    protected static String usernameRegex = "^[a-zA-Z0-9_]{1,16}";
    protected static String quantityRegex="^[0-9]{1,16}";

    public static String shopName;
    public static boolean recording=false;
    protected static List<String> invalidSignsLocation=new ArrayList<>();
    protected static JsonArray savedItems=new JsonArray();
    protected static int savedShopIndex;
    protected static int savedServerIndex;
    public static boolean isValid;
    public static String invalidReason;

    @Override
    public void onInitialize() {
        try {
            FileReader shopDataFile = new FileReader("config/shopmanager/shopdata.json");
            JsonElement file=new JsonParser().parse(shopDataFile);
            if (file instanceof JsonNull) {
            } else {
                serversJson = (JsonObject) file;
            }
        } catch (FileNotFoundException e) {
            System.out.println("shopdata file not found Creating");

            File configDir = new File("config");
            try {configDir.mkdir();}catch (Exception e2){System.out.println("Error while Creating Config File" + e2);}
            File shopManagerDir = new File("config/shopmanager");
            try {shopManagerDir.mkdir();}catch (Exception e2){System.out.println("Error while Creating shopmanager File" + e2);}


            File datafile = new File("config/shopmanager/shopdata.json");
            try {
                datafile.createNewFile();
            } catch (IOException e2) {
                System.out.println("Error while Creating Data File" + e2);
            }


        }
        if (serversJson.get("servers")==null){
            JsonArray serversarray=new JsonArray();
            serversJson.add("servers",serversarray);
        }
        System.out.println("On init :13");



    }


    public static void addCheckAndShop(SignBlockEntity signBlockEntity){

        String userName=null;
        String itemAmount=null;
        String itemName=null;
        String buyPrice=null;
        String sellPrice=null;
        String Location=null;
        String serverIp = null;
        String serverName = null;
        if (MinecraftClient.getInstance().isIntegratedServerRunning()){//get single player name
            serverName=MinecraftClient.getInstance().getServer().getServerName();
            serverIp="IntegratedServer."+serverName;
        }else if (MinecraftClient.getInstance().getCurrentServerEntry()!=null){//get multiplayer name and ip
            serverName=MinecraftClient.getInstance().getCurrentServerEntry().name;
            serverIp=MinecraftClient.getInstance().getCurrentServerEntry().address;
        }else{
            return;
        }

        signType=getSignType(signBlockEntity);
        Location=signBlockEntity.getPos().getX()+","+signBlockEntity.getPos().getY()+","+signBlockEntity.getPos().getZ()+","+MinecraftClient.getInstance().player.dimension.getRawId();
        isValid=true;
        switch (signType){
            case -1:
                isValid=false;
                if (!invalidSignsLocation.contains(Location)){
                    invalidSignsLocation.add(Location);
                    System.out.println("invalid Sign @ "+Location+" Reason: "+invalidReason+" -text: "+signBlockEntity.text[0].getString()+" - "+signBlockEntity.text[1].getString()+" - "+signBlockEntity.text[2].getString()+" - "+signBlockEntity.text[3].getString());
                }
                return;
            case 0:
                userName=signBlockEntity.text[0].getString();
                itemAmount=signBlockEntity.text[1].getString();
                buyPrice=signBlockEntity.text[2].getString().split(":")[0].replace("B","").replace(":","");
                sellPrice=signBlockEntity.text[2].getString().split(":")[1].replace("S","").replace(":","");
                itemName=signBlockEntity.text[3].getString();
                break;
            case 1:
                userName=signBlockEntity.text[0].getString();
                itemAmount=signBlockEntity.text[1].getString();
                sellPrice=signBlockEntity.text[2].getString().split(":")[0].replace("S","").replace(":","");
                buyPrice=signBlockEntity.text[2].getString().split(":")[1].replace("B","").replace(":","");
                itemName=signBlockEntity.text[3].getString();
                break;
            case 2:
                userName=signBlockEntity.text[0].getString();
                itemAmount=signBlockEntity.text[1].getString();
                buyPrice=signBlockEntity.text[2].getString().replace("B","");
                sellPrice="";
                itemName=signBlockEntity.text[3].getString();
                break;
            case 3:
                userName=signBlockEntity.text[0].getString();
                itemAmount=signBlockEntity.text[1].getString();
                buyPrice="";
                sellPrice=signBlockEntity.text[2].getString().replace("S","");
                itemName=signBlockEntity.text[3].getString();
                break;

            default:
                return;
        }

        JsonObject sign=new JsonObject();
        sign.addProperty("itemName",itemName);
        sign.addProperty("userName",userName);
        sign.addProperty("signType",signType);
        sign.addProperty("buyPrice",buyPrice);
        sign.addProperty("sellPrice",sellPrice);
        sign.addProperty("itemAmount",itemAmount);
        sign.addProperty("location",Location);

        if (!serverIp.equals(savedIp)){//search for existing server
            for (int x=0;x<serversJson.getAsJsonArray("servers").size();x++) {
                savedServerIndex =x;
                JsonElement server = serversJson.getAsJsonArray("servers").get(x);
                if (((JsonObject)server).get("serverIp").getAsString().equals(serverIp)){
                    savedIp=serverIp;
                    savedServer=(JsonObject)server;
                    System.out.println("Found server: " +" @ index: "+savedServerIndex);
                    break;
                }
            }
        }
        if (!serverIp.equals(savedIp)) {//create server with null shops
            savedServerIndex =-1;
            savedServer=new JsonObject();
            savedServer.addProperty("serverIp",serverIp);
            savedServer.addProperty("serverName",serverName);
            savedServer.add("shops",new JsonArray());
            savedIp=serverIp;
            System.out.println("created server: "+savedServer.toString());
        }

        if (!shopName.equals(savedShopName)){//search for existing shop
            for (int x=0;x<savedServer.getAsJsonArray("shops").size();x++){
                savedShopIndex =x;
                JsonElement shop =savedServer.getAsJsonArray("shops").get(x);
                if (shopName.equals(((JsonObject) shop).get("shopName").getAsString())){
                    savedshop=(JsonObject) shop;
                    savedShopName =shopName;
                    savedItems=new JsonArray();
                    savedshop.remove("items");
                    System.out.println("Found shop: "+" @ index:"+savedShopIndex);
                    break;
                }
            }
        }
        if (!shopName.equals(savedShopName)){//create shop with null items
            savedShopIndex =-1;
            savedshop=new JsonObject();
            savedItems=new JsonArray();
            savedshop.addProperty("shopName",ShopManager.shopName);
            savedshop.addProperty("boundingbox","");
            savedShopName =shopName;
            System.out.println("Created shop: "+savedshop.toString());
        }

        boolean signStored=false;
        for (int x=0;x<savedItems.size();x++) {
            JsonElement item=savedItems.get(x);
            if (sign.get("location").equals(((JsonObject) item).get("location"))){
                signStored=true;
                break;
            }
        }
        if (!signStored){
            System.out.println("adding sign @ "+sign.get("location"));
            savedItems.add(sign);
        }


/*
servers:[
    serverIp:"200.14.20.5",serverName:"My Test Server",shops:[
        shopName:"chaos 511's shop",boundingbox:?,items[
            itemName:"Diamond Sword",signType:1,buyPrice:"50",sellPrice:"",itemAmount:"1",location:"10,65,40,0",username:"chaoslord511"
        ]
    ]
]


 */

    }

    public static boolean saveserver(){
        String serverIp = null;
        String serverName = null;
        if (MinecraftClient.getInstance().isIntegratedServerRunning()){//get single player name
            serverName=MinecraftClient.getInstance().getServer().getServerName();
            serverIp="IntegratedServer."+serverName;
        }else if (MinecraftClient.getInstance().getCurrentServerEntry()!=null){//get multiplayer name and ip
            serverName=MinecraftClient.getInstance().getCurrentServerEntry().name;
            serverIp=MinecraftClient.getInstance().getCurrentServerEntry().address;
        }else{
            return false;
        }
        if (!serverIp.equals(savedIp)){//search for existing server
            for (int x=0;x<serversJson.getAsJsonArray("servers").size();x++) {
                savedServerIndex =x;
                JsonElement server = serversJson.getAsJsonArray("servers").get(x);
                if (((JsonObject)server).get("serverIp").getAsString().equals(serverIp)){
                    savedIp=serverIp;
                    savedServer=(JsonObject)server;
                    System.out.println("Found server: "+" @ index: "+savedServerIndex);
                    return true;
                }
            }
        }else{
            return true;
        }
        return false;
    }

    public static void storeshop(){
        if (savedItems.size()>0) {
            if (savedServerIndex != -1) {
                serversJson.getAsJsonArray("servers").remove(savedServerIndex);
            }
            if (savedShopIndex != -1) {
                savedServer.getAsJsonArray("shops").remove(savedShopIndex);
            }
            savedshop.add("items", savedItems);
            savedServer.getAsJsonArray("shops").add(savedshop);
            serversJson.getAsJsonArray("servers").add(savedServer);
            FileWriter datafile = null;
            if (serversJson != null) {
                try {
                    datafile = new FileWriter("config/shopmanager/shopdata.json");
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    datafile.write(gson.toJson(serversJson));
                    datafile.flush();
                    datafile.close();
                } catch (IOException e) {
                    System.out.println("Failed to Write to file " + e);
                }
            }
        }
    }

    private static int getSignType(SignBlockEntity signBlockEntity){
        if (signBlockEntity.text[0].getString().matches(usernameRegex)){//line one valid username
            if (signBlockEntity.text[1].getString().matches(quantityRegex)) {//line 2 quantity

                if (signBlockEntity.text[2].getString().split(":").length==2) {//line two price
                    if (signBlockEntity.text[2].getString().split(":")[0].contains("B")&&signBlockEntity.text[2].getString().split(":")[1].contains("S")){
                        String buyprice=signBlockEntity.text[2].getString().split(":")[0].replaceFirst("B","").replace(" ","");
                        String sellprice=signBlockEntity.text[2].getString().split(":")[1].replaceFirst("S","").replace(" ","");
                        if (buyprice.matches("[0-9]{0,32}.[0-9]{0,32}")) {
                            if (sellprice.matches("[0-9]{0,32}.[0-9]{0,32}")) {
                                return 0;
                            }
                        }
                    }else if (signBlockEntity.text[2].getString().split(":")[1].contains("B")&&signBlockEntity.text[2].getString().split(":")[0].contains("S")){
                        String buyprice=signBlockEntity.text[2].getString().split(":")[1].replaceFirst("B","").replace(" ","");
                        String sellprice=signBlockEntity.text[2].getString().split(":")[0].replaceFirst("S","").replace(" ","");
                        if (buyprice.matches("[0-9]{0,32}.[0-9]{0,32}")) {
                            if (sellprice.matches("[0-9]{0,32}.[0-9]{0,32}")) {
                                return 1;
                            }else{invalidReason="Regex fail line: 3";}
                        }else{invalidReason="Regex fail line: 3";}
                    }
                }else if (signBlockEntity.text[2].getString().split(":").length<2){
                    if (signBlockEntity.text[2].getString().contains("B")){
                        String buyprice=signBlockEntity.text[2].getString().replaceFirst("B","").replace(" ","");
                        if (buyprice.matches("[0-9]{0,32}.[0-9]{0,32}")) {
                            return 2;
                        }else{invalidReason="Regex fail line: 3";}
                    }else if (signBlockEntity.text[2].getString().contains("S")){
                        String sellprice=signBlockEntity.text[2].getString().replaceFirst("S","").replace(" ","");
                        if (sellprice.matches("[0-9]{0,32}.[0-9]{0,32}")) {
                            return 3;
                        }else{invalidReason="Regex fail line: 3";}
                    }else{invalidReason="Regex fail line: 3";}
                }else{invalidReason="Regex fail line: 3";}
            }else{invalidReason="Regex fail line: 2";}
        }else{invalidReason="Regex fail line: 1";}
        return -1;
    }


    /*
    types of sign shops
        1/2{
            {player id/shop id}
            {number of items}
            B {buy price}:(optinal) {sell price} S
            {Item id}
        },

     */
}