/* Leveraging Community detection for more Accurate Trust Prediction
/* Ghazaleh Beigi, Mahdi Jalili, Hamidreza Alvari, Gita Sukthankar
/* Sharif University of Technology 
/* University of Central Florida
/* Corresponding Author: Ghazaleh Beigi 
/* Data filtering code
*/

package datafilter;
import java.util.*;
import java.io.*;



public class Main {

    static Vector users, items, tempusers, tempitems;
    static int trusts,reviews;


    public static void main(String[] args) throws Exception{
        Main m = new Main();

        m.readTrustNetwork("./resources/trusts.txt");
        m.readRatings("./resources/ratings.txt");
        m.filter("./resources/trusts-new.txt","./resources/ratings-new.txt" );
    }



    void readTrustNetwork(String path)throws Exception{
        try{
            FileReader f = new FileReader(path);
            BufferedReader in = new BufferedReader(f);
            String s = in.readLine();
            users = new Vector();
            tempusers = new Vector();

            trusts = 0;

            String[] temp_edges = new String[2];
            String temp="";
            int count;


            while(s != null){
                count = 0;
                for(int i=0;i<s.length();i++){
                    if(s.charAt(i)!=' ' && s.charAt(i)!=',' && s.charAt(i)!='\t')
                       temp += s.charAt(i);
                    if(!temp.isEmpty())
                    if(s.charAt(i) == ' ' || s.charAt(i) == '\t' || s.charAt(i) == ',' || i==s.length()-1){
                        temp_edges[count] = temp;
                        count++;
                        temp = "";
                    }
                }


                if (!tempusers.contains(temp_edges[0])){
                    tempusers.addElement(temp_edges[0]);
                    user u = new user(temp_edges[0]);
                    u.trustees.addElement(temp_edges[1]);
                    users.addElement(u);
                }
                else{
                    int ind = -1;
                    for(int i=0;i<users.size();i++){
                        if( ((user)users.elementAt(i)).id.equals( temp_edges[0] ) ){
                            ind = i;
                        }
                    }
                    if (ind != -1)
                        ((user)users.elementAt(ind)).trustees.addElement(temp_edges[1]);
                }


                if (!tempusers.contains(temp_edges[1])){
                    tempusers.addElement(temp_edges[1]);
                    user u = new user(temp_edges[1]);
                    u.trustors.addElement(temp_edges[0]);
                    users.addElement(u);
                }
                else{
                    int ind = -1;
                    for(int i=0;i<users.size();i++){
                        if( ((user)users.elementAt(i)).id.equals(temp_edges[1]) ){
                            ind = i;
                        }
                    }
                    if (ind != -1)
                        ((user)users.elementAt(ind)).trustors.addElement(temp_edges[0]);
                }
                temp_edges = new String[2];
                s = in.readLine();
                trusts++;
            }

            f.close();
        }
        catch(FileNotFoundException err){
            System.out.println("No such file or directory!Try another file!Thanks...! *GH*");
            System.exit(1);
        }
   }



    void readRatings(String path)throws Exception{
       try{
            FileReader f = new FileReader(path);
            BufferedReader in = new BufferedReader(f);
            String s = in.readLine();

            String[] temp_edges = new String[3];
            String temp="";
            int count;
            
            items = new Vector();
            tempitems = new Vector();

            reviews = 0;
            while(s != null){
                count = 0;
                for(int i=0;i<s.length();i++){
                    if(s.charAt(i)!=' ' && s.charAt(i)!=',' && s.charAt(i)!='\t')
                       temp += s.charAt(i);
                    if(!temp.isEmpty())
                    if(s.charAt(i) == ' ' || s.charAt(i) == '\t' || s.charAt(i) == ',' || i==s.length()-1){
                        temp_edges[count] = temp;
                        count++;
                        temp = "";
                    }
                }


                if (!tempusers.contains(temp_edges[0])){
                    tempusers.addElement(temp_edges[0]);
                    user u = new user(temp_edges[0]);
                    u.items.addElement(temp_edges[1]);
                    u.ratings.addElement(temp_edges[2]);
                    users.addElement(u);
                }
                else{
                    int ind = -1;
                    for(int i=0;i<users.size();i++){
                        if( ((user)users.elementAt(i)).id.equals( temp_edges[0]) ){
                            ind = i;
                        }
                    }
                    if (ind != -1){
                        ((user)users.elementAt(ind)).items.addElement(temp_edges[1]);
                        ((user)users.elementAt(ind)).ratings.addElement(temp_edges[2]);
                    }
                }




                if (!tempitems.contains(temp_edges[1])){
                    tempitems.addElement(temp_edges[1]);

                    item it = new item(temp_edges[1]);
                    it.raters.addElement(temp_edges[0]);
                    items.addElement(it);

                }
                else{
                    int ind = -1;
                    for(int i=0;i<items.size();i++){
                        if( ((item)items.elementAt(i)).id.equals(temp_edges[1]) ){
                            ind = i;
                        }
                    }
                    if (ind != -1){
                        ((item)items.elementAt(ind)).raters.addElement(temp_edges[0]);
                    }
                }


                temp_edges = new String[3];
                s = in.readLine();
                reviews++;

            }
            f.close();
        }
        catch(FileNotFoundException err){
            System.out.println("No such file or directory!Try another file!Thanks...! *GH*");
            System.exit(1);
        }
    }



    void filter(String out1Loc,String out2Loc) throws Exception{
        FileWriter f;
        BufferedWriter out;
        f = new FileWriter(out1Loc);
        out = new BufferedWriter(f);

        System.out.println("##################Before##########################");
        System.out.println("Users: "+tempusers.size());
        System.out.println("Items: "+tempitems.size());
        System.out.println("Trusts: "+trusts);
        System.out.println("Ratings: "+reviews);

        ////// User removal
        while(still()){
            
            for(int i=0;i<users.size();i++){
                user u = (user)users.elementAt(i);

                if (u.trustors.size() <= 2){

                    users.removeElementAt(i);

                    int ind = -1;
                    for(int j=0;j<tempusers.size();j++)
                        if( ((String)tempusers.elementAt(j)).equals( u.id ) )
                            ind = j;
                    if (ind != -1)
                        tempusers.removeElementAt(ind);



                    for(int j=0;j<u.trustors.size();j++){
                        for(int k=0;k<users.size();k++){
                            if(((user)users.elementAt(k)).id.equals(u.trustors.elementAt(j))){
                                ind = -1;
                                for(int g=0;g<((user)users.elementAt(k)).trustees.size();g++)
                                    if( ((String)((user)users.elementAt(k)).trustees.elementAt(g)).equals( u.id ) )
                                        ind = g;
                                if (ind != -1)
                                    ((user)users.elementAt(k)).trustees.removeElementAt(ind);
                            }

                        }
                    }



                    for(int j=0;j<u.trustees.size();j++){
                        for(int k=0;k<users.size();k++){
                            if(((user)users.elementAt(k)).id.equals(u.trustees.elementAt(j))){
                                ind = -1;
                                for(int g=0;g<((user)users.elementAt(k)).trustors.size();g++)
                                    if( ((String)((user)users.elementAt(k)).trustors.elementAt(g)).equals( u.id ) )
                                        ind = g;
                                if (ind != -1)
                                    ((user)users.elementAt(k)).trustors.removeElementAt(ind);
                            }

                        }
                    }

                } //if
            }
    }
        System.out.println("Users --- After(1): "+tempusers.size());

        for(int i=0;i<users.size();i++){
            user u = (user)users.elementAt(i);
            if (u.trustees.isEmpty()){
                users.removeElementAt(i);

                int ind = -1;
                for(int j=0;j<tempusers.size();j++)
                    if( ((String)tempusers.elementAt(j)).equals( u.id ) )
                        ind = j;
                if (ind != -1)
                    tempusers.removeElementAt(ind);
            }
        }
       
        System.out.println("Users --- After(2): "+tempusers.size());
        
        /////////////Item removal       
        for(int i=0;i<items.size();i++){
            
            item it = (item)items.elementAt(i);
            if (it.raters.size() <= 2){

                items.removeElementAt(i);

                int ind = -1;
                for(int j=0;j<tempitems.size();j++)
                    if( ((String)tempitems.elementAt(j)).equals( it.id ) )
                        ind = j;
                if (ind != -1)
                    tempitems.removeElementAt(ind);

                for (int j=0;j<users.size();j++){
                    user u = (user)users.elementAt(j);

                    ind = -1;
                    for(int k=0;k<u.items.size();k++){
                        if ( ((String)u.items.elementAt(k)).equals( it.id ) ){
                            ind = k;
                        }
                    }
                    if (ind != -1){
                        u.items.removeElementAt(ind);
                        u.ratings.removeElementAt(ind);
                    }
                }
            }
        }

        


        System.out.println("Users --- After(3): "+tempusers.size());

        for(int i=0;i<users.size();i++){
            user u = (user)users.elementAt(i);
            if (u.items.isEmpty()){
                users.removeElementAt(i);

                int ind = -1;
                for(int j=0;j<tempusers.size();j++)
                    if( ((String)tempusers.elementAt(j)).equals( u.id ) )
                        ind = j;
                if (ind != -1)
                    tempusers.removeElementAt(ind);
            }
        }


        System.out.println("Users --- After(4): "+tempusers.size());

        trusts = 0;
        for(int i=0;i<users.size();i++){
            user u = (user)users.elementAt(i);
            for(int j=0;j<u.trustees.size();j++){
                out.append(u.id+"\t"+u.trustees.elementAt(j));
                out.newLine();
                trusts++;
            }
        }


        out.flush();
        out.close();
        f.close();



        f = new FileWriter(out2Loc);
        out = new BufferedWriter(f);

        reviews = 0;
        for(int i=0;i<users.size();i++){
            user u = (user)users.elementAt(i);
            for(int j=0;j<u.items.size();j++){
                out.append(u.id+"\t"+u.items.elementAt(j)+"\t"+u.ratings.elementAt(j));
                out.newLine();
                reviews++;
            }
        }

        out.flush();
        out.close();
        f.close();

        System.out.println("##################After##########################");
        System.out.println("Users --- (AFter 5): "+tempusers.size());
        System.out.println("Items: "+tempitems.size());
        System.out.println("Trusts: "+trusts);
        System.out.println("Ratings: "+reviews);

    }



    boolean still(){
        for(int i=0;i<users.size();i++){
            user u = (user)users.elementAt(i);
            if (u.trustors.size() <= 2)
                return true;
        }

        return false;
    }

}



class user{
    String id;
    Vector trustors = new Vector();
    Vector trustees = new Vector();
    Vector ratings = new Vector();
    Vector items = new Vector();

    user(String id){
        this.id = id;
    }
}


class item{
    String id;
    Vector raters = new Vector();
    
    item(String id){
        this.id = id;
    }
}