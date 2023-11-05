import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/*
 * Reference : https://arpitbhayani.me/blogs/consistent-hashing/
 */

class DEFAULT_NODE{
    static int DEFAULT_NODE = 7;
}
public class Main {
    public static void main(String[] args) throws Exception {
        ConsistentHash consistentHash = new ConsistentHash();
        List<ServerNode> serverNodes = new ArrayList<>();
        ServerNode nodeA = new ServerNode("A", "239.67.52.72");
        ServerNode nodeB = new ServerNode("B", "137.70.131.229");
        ServerNode nodeC = new ServerNode("C", "98.5.87.182");
        ServerNode nodeD = new ServerNode("D", "11.225.158.95");
        ServerNode nodeE = new ServerNode("E", "203.187.116.210");
        ServerNode nodeF = new ServerNode("F", "107.117.238.203");
        ServerNode nodeG = new ServerNode("G", "27.161.219.131");

        serverNodes.add(nodeA);
        serverNodes.add(nodeB);
        serverNodes.add(nodeC);
        serverNodes.add(nodeD);
        serverNodes.add(nodeE);
        serverNodes.add(nodeF);
        serverNodes.add(nodeG);

        for(ServerNode node : serverNodes){
            ConsistentHash.addNode(node);
        }

        // saving into server after getting.
        RequestUrl requestUrl1 = new RequestUrl("https://google.com");
        RequestUrl requestUrl2 = new RequestUrl("https://netflix.com");
        RequestUrl requestUrl3 = new RequestUrl("https://youtube.com");
        RequestUrl requestUrl4 = new RequestUrl("https://amneet.com");
        RequestUrl requestUrl5 = new RequestUrl("https://priyam.com");

        ConsistentHash.assign(requestUrl1).putFile(requestUrl1);// on which server it stores.
        ConsistentHash.assign(requestUrl2).putFile(requestUrl2);// on which server it stores.
        ConsistentHash.assign(requestUrl3).putFile(requestUrl3);// on which server it stores.
        ConsistentHash.assign(requestUrl4).putFile(requestUrl4);// on which server it stores.
        ConsistentHash.assign(requestUrl5).putFile(requestUrl5);// on which server it stores.

//
//        // if we are removing server 6, all data of 6 get pushed to next... that is one.
//        ConsistentHash.deleteNode(nodeC);


        // add new node, Server-H,,       ServerC data which hash < ServerH's hash get transfered to ServerH

        ServerNode nodeH = new ServerNode("H", "27.161.2131.11");
        ConsistentHash.addNode(nodeH);


    }
}

class ConsistentHash{
    private static List<Integer> keys;// particular node's position in hashmap.
    private static List<ServerNode> nodes; // server nodes.
    public ConsistentHash(){
        keys = new ArrayList<>();
        nodes = new ArrayList<>();
    }

    public static void addNode(ServerNode node) throws Exception {
        System.out.println("----  Before ---- ");
        printvalues();
        int key = node.hash;
        /*
         * find the index where the key should be inserted in the keys array
         * this will be the index where the Storage Node will be added in the
         * nodes array.
         */
        int index = Bisect.bisect(keys,key);

        /*
         * insert the node_id and the key at the same `index` location.
         * this insertion will keep nodes and keys sorted w.r.t key
         */

        //TODO: Data Migration
        if(keys.size()>= DEFAULT_NODE.DEFAULT_NODE){
            dataMigrationForNewNode(node,nodes.get(index));
        }
        nodes.add(index,node);
        keys.add(index,key);
        System.out.println("----  After ---- ");
        printvalues();
    }

    public static void deleteNode(ServerNode node) throws Exception {
        System.out.println("----  Before ---- ");
        printvalues();
        int key = node.hash;
        /*
         * find the index where the key should be inserted in the keys array
         * this will be the index where the Storage Node will be added in the
         * nodes array.
         */
        int index = Bisect.bisectLeft(keys,key);

        /*
         * remove the node_id and the key at the same `index` location.
         * this removal will keep nodes and keys sorted w.r.t key
         */
        //TODO: Data Migration
        int next = (index + 1)%keys.size();
        dataMigration(nodes.get(index),nodes.get(next));
        nodes.remove(index);
        keys.remove(index);
        System.out.println("----  After ---- ");
        printvalues();
    }

    public static ServerNode assign(RequestUrl url) throws Exception {
        System.out.println("----  Before ---- ");
        printvalues();
        int key = url.hash;
        /*
         *  # we find the first node to the right of this key
            # if bisect_right returns index which is out of bounds then
            # we circle back to the first in the array in a circular fashion.
         */
        int index = Bisect.bisectLeft(keys,key) % keys.size();
        System.out.println("Index of assign:: "+ index +" Its hash ::"+ key );
        return nodes.get(index);
    }


    public static void printvalues(){
        System.out.println("----- Keys ------");
        for(Integer key : keys){
            System.out.print(key+ ", ");
        }
        System.out.println(" ");
        System.out.println("----- Nodes ------");
        for(ServerNode node : nodes){
            System.out.print(node.name+ ", ");
        }

        System.out.println(" ");
    }

    public static void dataMigration(ServerNode node1 , ServerNode node2){
        System.out.println("Do Data Migration From Node1: "+ node1.name + " TO :: NODE2 : "+ node2.name);
        for (Map.Entry<String, RequestUrl> entry : node1.fileDb.entrySet()) {
            RequestUrl value = entry.getValue();
            node2.putFile(value);
        }

        System.out.println(" ------- Data migration completed ----------");
        printvalues();
    }

    public static void dataMigrationForNewNode(ServerNode node1 , ServerNode node2){
        System.out.println("Do Data Migration From Node1: "+ node1.name + " TO :: NODE2 : "+ node2.name);
        List<String> removeUrl = new ArrayList<>();
        for (Map.Entry<String, RequestUrl> entry : node2.fileDb.entrySet()) {
            RequestUrl value = entry.getValue();
            if(value.hash <= node1.hash){
                node1.putFile(value);
                removeUrl.add(value.url);
            }
        }
        for(String url : removeUrl){
            node2.fileDb.remove(url);
        }
        System.out.println(" ------- Data migration completed ----------");
        printvalues();
    }
}

class HashGenerator{
    static int TOTAL_SLOTS = 150;
    public static int hashFn(String path){
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = path.getBytes(StandardCharsets.UTF_8);
            byte[] hashBytes = md.digest(keyBytes);
            BigInteger hashValue = new BigInteger(1, hashBytes);
            return hashValue.mod(BigInteger.valueOf(TOTAL_SLOTS)).intValue();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return -1; // Handle the exception appropriately
        }
    }
}

class ServerNode{
    String name;
    String host;

    int hash;

    Map<String,RequestUrl> fileDb;
    public ServerNode(String name, String host){
        this.name = name;
        this.host = host;
        fileDb = new HashMap<>();
        this.hash = HashGenerator.hashFn(this.host);
    }

    public RequestUrl fetchFile(RequestUrl value){
        System.out.println("----  Before ---- ");
        println();
        return fileDb.get(value.url);
    }

    public void putFile(RequestUrl value){
        fileDb.put(value.url, value);
        System.out.println("----  After ---- ");
        println();
    }

    public void println(){
        System.out.println("------ Server Details ----------");
        System.out.println("Name : "+ name+ " Host : "+  host);
        for (Map.Entry<String, RequestUrl> entry : fileDb.entrySet()) {
            String key = entry.getKey();
            RequestUrl value = entry.getValue();
            System.out.println("Key: " + key + ", Value: " + value.hash + " , "+ value.url);
        }
    }
}

class RequestUrl{
    String url ;
    int hash;
    public RequestUrl(String url){
        this.url = url;
        this.hash = HashGenerator.hashFn(this.url);
    }
}

class Bisect{
    // This I want LowerBound
    public static int bisect(List<Integer> keys, int key) throws Exception {
        int left = 0;
        int right = keys.size()-1;
        int ans =0;
        while (left <= right) {
            int mid = (left+right)/2;
            if(keys.get(mid) == key){
                throw new Exception("collision occurred");
            }
            if(keys.get(mid) < key){
                left = mid + 1;
                ans = left;
            }
            else{
                ans = mid; // one greater.
                right = mid - 1;
            }
        }
        return ans; // Insertion point for the key
    }

    public static int bisectLeft(List<Integer> keys, int key) throws Exception {
        int left = 0;
        int right = keys.size()-1;
        int ans = 0;
        while (left <= right) {
            int mid = (left+right)/2;
            if(keys.get(mid) == key){
                return mid;
            }
            if(keys.get(mid)< key){
                left = mid + 1;
                ans = left;
            }
            else{
                ans = mid; // one greater.
                right = mid - 1;
            }
        }
        return ans; // Insertion point for the key
    }

}
