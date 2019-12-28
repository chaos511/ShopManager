package net.chaos511.shopmanager;

import com.google.gson.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


public class Command {
    private static DecimalFormat df = new DecimalFormat("0.00");

    private static String[] commands=new String[]{
        "list",
        "startrec",
        "stoprec",
        "iteminfo",
        "sell",
        "buy",
        "crash",
        "help"
    };
    private static String helpmsg="/sm startrec <shopname>: start recording shop prices" +"\n"+
            "/sm stoprec: stop recording shop prices" +"\n"+
            "/sm iteminfo: shos the truncated sign name of what is in the main hand" +"\n"+
            "/sm sell <itemname|hand> <quantity>: shows all the shops where the item can be sold" +"\n"+
            "/sm buy <itemname|hand> <quantity>: shows all the shops where the item can be bought" +"\n"+
            "/crash: throws a index out of range exception" +"\n"+
            "/help: this message" +"\n"+
            "";
    private static String[] prefixs=new String[]{
            "shopmanager",
            "sm"
    };
    public static boolean isPrefix(String prefix){
        if (prefix.startsWith("/")){prefix=prefix.substring(1);}else {return false;}
        for(String prfix:prefixs){
            if (prefix.equals(prfix)){return true;}
        }
        return false;
    }
    public static boolean isCommand(String command){
        return getcommandindex(command)!=-1;
    }
    private static int getcommandindex(String command){
        for(int x=0;x<commands.length;x++) {
            String cmand=commands[x];
            if (command.equals(cmand)){
                return x;
            }
        }
        return -1;
    }
    public static String execCommand(String prefix,String command,String[] args){
        StringBuilder argsAsString= new StringBuilder();
        for (int x=2;x<args.length;x++){
            argsAsString.append(args[x]);
        }
        Item item=null;
        int amount=1;
        switch (getcommandindex(command)){
            case 0://list
                if (ShopManager.serversJson !=null){
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    return gson.toJson(ShopManager.serversJson);
                }else {
                    return "Server List null";
                }
            case 1://startrec
                if (argsAsString.toString().length()>0) {
                    ShopManager.savedItems=new JsonArray();
                    ShopManager.shopName = argsAsString.toString();
                    ShopManager.recording = true;
                    ShopManager.invalidSignsLocation=new ArrayList<>();
                    ShopManager.savedShopName="";
                    ShopManager.savedIp="";
                    return "started recording prices for shop: "+ShopManager.shopName;
                }else {
                    return "Usage /shopmanager startrec <ShopName>";
                }
            case 2://stoprec
                if (ShopManager.recording) {
                    String shopname=ShopManager.shopName;
                    int invalid = ShopManager.invalidSignsLocation.size();
                    int valid = ShopManager.savedItems.size();
                    ShopManager.storeshop();
                    ShopManager.shopName = null;
                    ShopManager.recording = false;
                    return "shop recording for shop: " + shopname + "\n Invalid Signs Found: " + invalid + "\n Valid Signs Found: " + valid;
                }
                return "Recording not started";
            case 3://iteminfo
                return Util.getName(MinecraftClient.getInstance().player.getMainHandStack(),Util.getStringWidth("---------------"));
            case 4://sel
                item=null;
                amount=1;
                if (args.length==2){
                    item=MinecraftClient.getInstance().player.getMainHandStack().getItem();
                    amount=MinecraftClient.getInstance().player.getMainHandStack().getCount();
                }else if (args.length==3||args.length==4){
                    System.out.println("args[2]: "+args[2]);
                    if (args[2].equals("hand")){
                        item=MinecraftClient.getInstance().player.getMainHandStack().getItem();
                        amount = MinecraftClient.getInstance().player.getMainHandStack().getCount();
                    }else{

                        item = Registry.ITEM.get(new Identifier(args[2]));
                        if (item.getName().toString().equalsIgnoreCase("air")) {
                            return "invalid item id: " + args[2];
                        }

                        System.out.println(item);
                    }

                    if (args.length==4) {
                        try {
                            amount = Integer.parseInt(args[3]);
                        } catch (NumberFormatException e) {
                            return "Usage /shopmanager sell <itemname|hand> <quantity>";
                        }
                    }

                }else {return "Usage /shopmanager sell <itemname|hand> <quantity>";}
                return getSellPrice(item,amount);
            case 5://buy
                item=null;
                amount=1;
                System.out.println("args len: "+args.length);
                if (args.length==2){
                    item=MinecraftClient.getInstance().player.getMainHandStack().getItem();
                    amount=MinecraftClient.getInstance().player.getMainHandStack().getCount();
                }else if (args.length==3||args.length==4){
                    if (args[2].equals("hand")){
                        item=MinecraftClient.getInstance().player.getMainHandStack().getItem();
                        amount = MinecraftClient.getInstance().player.getMainHandStack().getCount();
                    }else{
                        item = Registry.ITEM.get(new Identifier(args[2]));
                        if (item.getName().toString().equalsIgnoreCase("air")) {
                            return "invalid item id: " + args[2];
                        }
                        System.out.println(item);
                    }

                    if (args.length==4) {
                        try {
                            amount = Integer.parseInt(args[3]);
                        } catch (NumberFormatException e) {
                            return "Usage /shopmanager sell <itemname|hand> <quantity>";
                        }
                    }

                }else {return "Usage /shopmanager buy <itemname|hand> <quantity>";}
                return getBuyPrice(item,amount);

            case 6://crash
                return args[args.length+1];
            case 7://help
                return helpmsg;
        }
        return "This should never be returned";
    }
    private static String getSellPrice(Item itemtofind,int quantity){
        try {
        StringBuilder returnstr=new StringBuilder();
        List<Float> prices=new ArrayList<>();
        List<Float> pricesunsorted=new ArrayList<>();
        List<String> items=new ArrayList<>();
        List<Integer> addedindexs =new ArrayList<>();

            String itemName=Util.getName(itemtofind.getStackForRender(),Util.getStringWidth("---------------"));
        int prefixlen=0;
        if (ShopManager.saveserver()){
            returnstr.append(quantity).append(" x ").append(itemName).append("\n");
            prefixlen = returnstr.length();
            for (JsonElement shop:ShopManager.savedServer.getAsJsonArray("shops")) {
                if(((JsonObject)shop).has("items")) {
                    for (JsonElement item : ((JsonObject) shop).getAsJsonArray("items")) {
                        try {
                            if (((JsonObject) item).get("itemName").getAsString().equals(itemName) && !((JsonObject) item).get("sellPrice").getAsString().isEmpty()) {
                                System.out.println("found: " + itemName + " @ shop: " + ((JsonObject) shop).get("shopName").getAsString() + " item: " + ((JsonObject) item).toString());
                                prices.add(getSellPriceperitem(item));
                                pricesunsorted.add(getSellPriceperitem(item));
                                items.add(df.format(getSellPriceperitem(item) * quantity) + " coins@[" + df.format(getSellPriceperitem(item)) + " per] shop amount:" + ((JsonObject) item).get("itemAmount").getAsString() + " @ shop: " + ((JsonObject) shop).get("shopName").getAsString() + "\n");
                            }
                        } catch (Exception ignore) {
                        }
                    }
                }
            }
        }
        if (items.size()==0){
            returnstr.append("Found at no shops");
        }else{

            prices.sort(Collections.reverseOrder());
            for (Float price : prices) {
                for (int y = 0; y < pricesunsorted.size(); y++) {
                    if (price.equals(pricesunsorted.get(y))&&!addedindexs.contains(y)) {
                        returnstr.append(items.get(y));
                        addedindexs.add(y);
                    }
                }
            }
        }
        return returnstr.toString();
    }catch (Exception e){
        return "Exception: "+e;
    }
}
    private static String getBuyPrice(Item itemtofind,int quantity){
        try {
            StringBuilder returnstr = new StringBuilder();
            List<Float> prices = new ArrayList<>();
            List<Float> pricesunsorted = new ArrayList<>();
            List<String> items = new ArrayList<>();
            List<Integer> addedindexs =new ArrayList<>();

            String itemName = Util.getName(itemtofind.getStackForRender(), Util.getStringWidth("---------------"));
            int prefixlen = 0;
            if (ShopManager.saveserver()) {
                returnstr.append(quantity).append(" x ").append(itemName).append("\n");
                prefixlen = returnstr.length();
                for (JsonElement shop : ShopManager.savedServer.getAsJsonArray("shops")) {
                    if(((JsonObject)shop).has("items")) {
                        for (JsonElement item : ((JsonObject) shop).getAsJsonArray("items")) {
                            try {
                                if (((JsonObject) item).get("itemName").getAsString().equals(itemName) && !((JsonObject) item).get("buyPrice").getAsString().isEmpty()) {
                                    prices.add(getBuyPriceperitem(item));
                                    pricesunsorted.add(getBuyPriceperitem(item));
                                    items.add(df.format(getBuyPriceperitem(item) * quantity) + " coins@[" + df.format(getBuyPriceperitem(item)) + " per] shop amount:" + ((JsonObject) item).get("itemAmount").getAsString() + " @ shop: " + ((JsonObject) shop).get("shopName").getAsString() + "\n");
                                }
                            } catch (Exception ignore) {
                            }
                        }
                    }
                }
            }
            if (items.size() == 0) {
                returnstr.append("Found at no shops");
            } else {
                Collections.sort(prices);
                for (Float price : prices) {
                    for (int y = 0; y < pricesunsorted.size(); y++) {
                        if (price.equals(pricesunsorted.get(y))&&!addedindexs.contains(y)) {
                            returnstr.append(items.get(y));
                            addedindexs.add(y);
                        }
                    }
                }
            }
            return returnstr.toString();
        }catch (Exception e){
            return "Exception: "+e;
        }

    }

    static float getSellPriceperitem(JsonElement item){
        return Float.parseFloat(((JsonObject) item).get("sellPrice").getAsString())/Float.parseFloat(((JsonObject) item).get("itemAmount").getAsString());
    }
    static float getBuyPriceperitem(JsonElement item){
        return Float.parseFloat(((JsonObject) item).get("buyPrice").getAsString())/Float.parseFloat(((JsonObject) item).get("itemAmount").getAsString());
    }
}
