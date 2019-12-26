package net.chaos511.shopmanager;

import net.minecraft.item.ItemStack;

public class Util {

    public static String getName(ItemStack itemStack, int maxWidth){
        String itemName = itemStack.getItem().getName().asString();
        System.out.println("name: "+itemName);
        String durability = "";
        String metaData = "";
        String code = itemName.substring(0, 1).toUpperCase() + itemName.substring(1);
        System.out.println("code: "+code);

        int codeWidth = getStringWidth(code + durability + metaData);
        if (maxWidth > 0 && codeWidth > maxWidth) {
            int exceeding = codeWidth - maxWidth;
            code = getShortenedName(code, getStringWidth(code) - exceeding);
            System.out.println("short code: "+code);
        }


        code += durability + metaData;
        return code;
    }



    private static String characters = " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_'abcdefghijklmnopqrstuvwxyz{|}~¦ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜø£Ø×áíóúñÑªº¿®¬½¼¡«»";
    private static int[] extraWidth = {4,2,5,6,6,6,6,3,5,5,5,6,2,6,2,6,6,6,6,6,6,6,6,6,6,6,2,2,5,6,5,6,7,6,6,6,6,6,6,6,6,4,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,4,6,4,6,6,3,6,6,6,6,6,5,6,6,2,6,5,3,6,6,6,6,6,6,6,4,6,6,6,6,6,6,5,2,5,7,6,6,6,6,6,6,6,6,6,6,6,6,4,6,3,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,4,6,6,3,6,6,6,6,6,6,6,7,6,6,6,2,6,6,8,9,9,6,6,6,8,8,6,8,8,8,8,8,6,6,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,6,9,9,9,5,9,9,8,7,7,8,7,8,8,8,7,8,8,7,9,9,6,7,7,7,7,7,9,6,7,8,7,6,6,9,7,6,7,1};


    public static int getCharWidth(char c) {
        //if (c != ChatColor.COLOR_CHAR) {
            int index = characters.indexOf(c);
            if (index > -1) {
                return extraWidth[index];
            } else {
                return 10;
            }
        //}
    }
    public static int getStringWidth(String string) {
        int width = 0;
        for (char c : string.toCharArray()) {
            width += getCharWidth(c);
        }
        return width;
    }
    public static String getShortenedName(String itemName, int maxWidth) {
        itemName = itemName.substring(0, 1).replace('_', ' ').toUpperCase() + itemName.substring(1).replace('_', ' ');
        int width = getStringWidth(itemName);
        if (width <= maxWidth) {
            return itemName;
        }
        String[] itemParts = itemName.split(" ");
        itemName = String.join("", itemParts);
        width = getStringWidth(itemName);
        if (width <= maxWidth) {
            return itemName;
        }
        int exceeding = width - maxWidth;
        int shortestIndex = 0;
        int longestIndex = 0;
        for (int i = 0; i < itemParts.length; i++) {
            if (getStringWidth(itemParts[longestIndex]) < getStringWidth(itemParts[i])) {
                longestIndex = i;
            }
            if (getStringWidth(itemParts[shortestIndex]) > getStringWidth(itemParts[i])) {
                shortestIndex = i;
            }
        }
        int shortestWidth = getStringWidth(itemParts[shortestIndex]);
        int longestWidth = getStringWidth(itemParts[longestIndex]);
        int remove = longestWidth - shortestWidth;
        while (remove > 0 && exceeding > 0) {
            int endWidth = getCharWidth(itemParts[longestIndex].charAt(itemParts[longestIndex].length() - 1));
            itemParts[longestIndex] = itemParts[longestIndex].substring(0, itemParts[longestIndex].length() - 1);
            remove -= endWidth;
            exceeding -= endWidth;
        }

        for (int i = itemParts.length - 1; i >= 0 && exceeding > 0; i--) {
            int partWidth = getStringWidth(itemParts[i]);

            if (partWidth > shortestWidth) {
                remove = partWidth - shortestWidth;
            }

            if (remove > exceeding) {
                remove = exceeding;
            }

            while (remove > 0) {
                int endWidth = getCharWidth(itemParts[i].charAt(itemParts[i].length() - 1));
                itemParts[i] = itemParts[i].substring(0, itemParts[i].length() - 1);
                remove -= endWidth;
                exceeding -= endWidth;
            }
        }

        while (exceeding > 0) {
            for (int i = itemParts.length - 1; i >= 0 && exceeding > 0; i--) {
                int endWidth = getCharWidth(itemParts[i].charAt(itemParts[i].length() - 1));
                itemParts[i] = itemParts[i].substring(0, itemParts[i].length() - 1);
                exceeding -= endWidth;
            }
        }
        return String.join("", itemParts);
    }
}
