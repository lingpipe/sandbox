import java.io.*;
import java.util.*;

public class Munge {

    static int sNextAnnoId = 1;
    static int sNextItemId = 1;

    public static void main(String[] args) throws Exception {
        // args[0] = "rte.standardized.tsv"
        InputStream in = new FileInputStream(args[0]);
        Reader reader = new InputStreamReader(in,"ISO-8859-1");
        BufferedReader buf = new BufferedReader(reader);

        Map<String,Integer> annoToId = new TreeMap<String,Integer>();
        Map<String,Integer> itemToId = new TreeMap<String,Integer>();
        Map<Integer,Integer> itemIdToGold = new TreeMap<Integer,Integer>();


        Writer dataWriter = new FileWriter("rte.data.tsv");
        // CALC & DATA OUTPUT
        buf.readLine(); // throw away first line
        String line = null;
        while ((line = buf.readLine()) != null) {
            String[] fields = line.split("\t");
            String anno = fields[1];
            String item = fields[2];
            String label = fields[3];
            String gold = fields[4];
            int annoId = id(annoToId,anno,true);
            int itemId = id(itemToId,item,false);
            itemIdToGold.put(itemId,Integer.parseInt(gold));
            dataWriter.write(itemId + "\t" + annoId + "\t" + label + "\n");
        }
        dataWriter.close();


        // GOLD OUTPUT
        Writer goldWriter = new FileWriter("rte.gold.tsv");
        for (Integer itemId : itemIdToGold.keySet()) {
            Integer gold = itemIdToGold.get(itemId);
            goldWriter.write(gold + "\n");
        }
        goldWriter.close();

        // ITEM-ID OUTPUT
        Writer itemIdWriter = new FileWriter("rte.item-id.tsv");
        for (String item : itemToId.keySet()) {
            Integer id = itemToId.get(item);
            itemIdWriter.write(item + "\t" + id + "\n");
        }
        itemIdWriter.close();

        // ID-ITEM OUTPUT
        Map<Integer,String> idToItem = new TreeMap<Integer,String>();
        for (String item : itemToId.keySet()) {
            Integer id = itemToId.get(item);
            idToItem.put(id,item);
        }
        Writer idItemWriter = new FileWriter("rte.id-item.tsv");
        for (Integer id: idToItem.keySet()) {
            String item = idToItem.get(id);
            idItemWriter.write(id + "\t" + item + "\n");
        }
        idItemWriter.close();

        // ANNO-ID OUTPUT
        Writer annoIdWriter = new FileWriter("rte.anno-id.tsv");
        for (String anno : annoToId.keySet()) {
            Integer id = annoToId.get(anno);
            annoIdWriter.write(anno + "\t" + id + "\n");
        }
        annoIdWriter.close();

    }

    static int id(Map<String,Integer> map, String name, boolean isAnno) {
        Integer id = map.get(name);
        if (id == null) {
            id = isAnno ? (sNextAnnoId++) : (sNextItemId++);
            map.put(name,id);
        }
        return id;
    }

}