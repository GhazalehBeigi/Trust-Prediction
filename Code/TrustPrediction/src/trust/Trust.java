/* Leveraging Community detection for more Accurate Trust Prediction
/* Ghazaleh Beigi, Mahdi Jalili, Hamidreza Alvari, Gita Sukthankar
/* Sharif University of Technology 
/* University of Central Florida
/* Corresponding Author: Ghazaleh Beigi 
/* Main trust prediction code
*/

package Trust;
import java.util.*;
import java.io.*;
import edu.uci.ics.jung.algorithms.importance.BetweennessCentrality;
import edu.uci.ics.jung.algorithms.scoring.EigenvectorCentrality;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;



public class Trust {


    static int n, m, iterations, method, selectedOption,limit, centrality, combination, maxTrustors,maxTrustees;
    static double alpha;
    static agent[] agents;
    static Vector communities, users, trusts, ratings, items, centers, N, BUN;
    static double[] max_of_skills;


    public static void main(String[] args) throws Exception{
        Trust tr = new Trust();
        
        String[] inputFile = tr.readConfig("./resources/config.txt");

        tr.readTrustNetwork(inputFile[0]);
        System.out.println("Reading trust network completed... "+users.size()+" users have been added to the list!");
        tr.readRatings(inputFile[1]);
        System.out.println("Reading reviews network completed..."+items.size()+" items have been added to the list!");
        System.out.println("Total number of users are now: "+users.size());
        
        tr.initialize();
        
        System.out.println("Max # of Trustors: "+maxTrustors);
        System.out.println("Max # of Trustees: "+maxTrustees);
        
        
        tr.similarity();
        System.out.println("Calculating trust similarities for trust relations with size #" + trusts.size()+" has been completed!");
        tr.ratingsDiversity(selectedOption);
        System.out.println("Calculating review similarities for reviews with size #" +ratings.size()+" has been completed!\n\n\n");


        int[] test = {50,60,70,80,90};
        double[] alphas = {0,0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1};
        
        
        for (int run=0;run<11;run++){
            alpha = alphas[run];
                        
            communities = new Vector();
            int[] temp;
            for(int i=0;i<users.size();i++){
                temp = new int[1];
                temp[0] = i + 1;
                communities.add(temp);
            }

            tr.Game();

            
            String[] homophily = {"-cosine-","-l1-","-l2-","-jaccard-"};
            String[] methods = {"-struct-","-homph-","-inv-","-neg-"+"-struct+homph-"+"-struct+inv-","-struct+neg-"};
            String[] centralities = {"-avg-","-betw-","-eigen-","-trustor","-trstee","-deg","-rand"};

            tr.printCommunities("./resources/community-alpha-"+alpha+"-method"+methods[method-1]+"homophily"+homophily[selectedOption - 1]+"centrality"+centralities[centrality - 1]+".txt");
            tr.centerDetection("./resources/centers-alpha-"+alpha+"-method"+methods[method-1]+"homophily"+homophily[selectedOption - 1]+"centrality"+centralities[centrality - 1]+".txt");
            
            
            String outLoc1 = "./resources/sortedPredict-"+test[0]+"%-alpha-"+alpha+"-method"+methods[method-1]+"homophily"+homophily[selectedOption - 1]+"centrality"+centralities[centrality - 1]+".txt";
            String outLoc2 = "./resources/accuracy-"+test[0]+"%-alpha-"+alpha+"-method"+methods[method-1]+"homophily"+homophily[selectedOption - 1]+"centrality"+centralities[centrality - 1]+".txt";
            tr.printAccuracy(outLoc2,tr.accuracyCalculate(test[0],outLoc1));

            for(int i=1;i<test.length;i++){
                outLoc1 = "./resources/sortedPredict-"+test[i]+"%-alpha-"+alpha+"-method"+methods[method-1]+"homophily"+homophily[selectedOption - 1]+"centrality"+centralities[centrality - 1]+".txt";
                outLoc2 = "./resources/accuracy-"+test[i]+"%-alpha-"+alpha+"-method"+methods[method-1]+"homophily"+homophily[selectedOption - 1]+"centrality"+centralities[centrality - 1]+".txt";
                tr.printAccuracy(outLoc2,tr.accuracyCalculate2(test[0],test[i],outLoc1));
            }

            for (int i=0;i<n;i++){
                Vector L = new Vector(1);
                L.addElement(-(i + 1)); //At first each agent has its own label
                agents[i].L.clear();
                agents[i].setL(L);
            }  
            
            System.out.println("*Running the code with alpha = "+alpha+" has been finished*");
        }

    }

    

    String[] readConfig(String configFile)throws Exception{
        FileReader f = new FileReader(configFile);
        BufferedReader in = new BufferedReader(f);

        String[] inputFiles = new String[2];
        String s;
        String[] val;
        for(int i=0;i<2;i++){
            s = in.readLine();
            val = s.split("=");
            inputFiles[i] = val[1];
        }

        s = in.readLine();
        val = s.split("=");
        if (val[1].equals("structure"))
            method = 1;
        else if (val[1].equals("homophily"))
            method = 2;
        else if (val[1].equals("inv-homophily"))
            method = 3;
        else if (val[1].equals("neg-homophily"))
            method = 4;
        else if (val[1].equals("structure+homophily"))
            method = 5;
        else if (val[1].equals("structure+inv-homophily"))
            method = 6;
        else if (val[1].equals("structure+neg-homophily"))
            method = 7;


        s = in.readLine();
        val = s.split("=");
        if(val[1].equals("cosine"))
           selectedOption = 1;
        else if(val[1].equals("l1"))
            selectedOption = 2;
        else if(val[1].equals("l2"))
            selectedOption = 3;
        else if(val[1].equals("jaccard"))
            selectedOption = 4;


        s = in.readLine();
        val = s.split("=");
        if(val[1].equals("average"))
           centrality = 1;
        else if(val[1].equals("betweenness"))
            centrality = 2;
        else if(val[1].equals("eigenvector"))
            centrality = 3;
        else if (val[1].equals("maxtrustor"))
            centrality = 4;
        else if (val[1].equals("maxtrustee"))
            centrality = 5;
        else if (val[1].equals("maxdegree"))
            centrality = 6;
        else if (val[1].equals("random"))
            centrality = 7;
        
        
        
        s = in.readLine();
        val = s.split("=");
        if(val[1].equals("averaging"))
           combination = 1;
        else if(val[1].equals("multiplying"))
           combination = 2;
        

        return inputFiles;
    }

    
    
    
    void readTrustNetwork(String path)throws Exception{
        try{
            FileReader f = new FileReader(path);
            BufferedReader in = new BufferedReader(f);
            String s = in.readLine();
            trusts = new Vector(1);
            users = new Vector();
            
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
                trusts.addElement(temp_edges);
                
                if (!users.contains(temp_edges[0]))
                    users.addElement(temp_edges[0]);

                if (!users.contains(temp_edges[1]))
                    users.addElement(temp_edges[1]);
                
                
                temp_edges = new String[2];
                s = in.readLine();
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
            ratings = new Vector();
            items = new Vector();
            
            String[] temp_edges = new String[3];
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

                ratings.addElement(temp_edges);
                

                if (!users.contains(temp_edges[0]))
                    users.addElement(temp_edges[0]);
                
                if (!items.contains( temp_edges[1] ))
                    items.addElement( temp_edges[1] );

                temp_edges = new String[3];
                s = in.readLine();

            }
            f.close();

        }
        catch(FileNotFoundException err){
            System.out.println("No such file or directory!Try another file!Thanks...! *GH*");
            System.exit(1);
        }
    }

    
    
    void initialize(){
        n = users.size();

        agents = new agent[n];
        for (int i=0;i<n;i++){
            Vector L = new Vector(1);
            L.addElement(-(i + 1)); //At first each agent has its own label
            agents[i] = new agent(i+1);
            agents[i].setL(L);
        }

        String[] temp_edges;

        for(int i=0; i<trusts.size(); i++){
            temp_edges = (String[])trusts.elementAt(i);
            int ind1 = found(users,temp_edges[0]);
            int ind2 = found(users, temp_edges[1]);
            if(ind1 != -1 && ind2 != -1){
                agents[ind1].nexts.addElement(ind2);
                agents[ind1].trustees.addElement(ind2);
                agents[ind1].deg++;
                agents[ind2].nexts.addElement(ind1);
                agents[ind2].trustors.addElement(ind1);
                agents[ind2].deg++;
            }
        }
        String[] tempp;
        int ind;
        for(int i=0; i<ratings.size(); i++){
            tempp = (String[])ratings.elementAt(i);
            ind = found(users, tempp[0]);
            agents[ind].items.add( Integer.parseInt( tempp[1] ) );
            agents[ind].ratings.add( Double.parseDouble( tempp[2] ) );
        }
        
        

        m = trusts.size();
        
        maxTrustees = agents[0].trustees.size();
        maxTrustors = agents[0].trustors.size();
        
        for(int i=1;i<agents.length;i++){
            if (agents[i].trustees.size() > maxTrustees)
                maxTrustees = agents[i].trustees.size();
            
            if (agents[i].trustors.size() > maxTrustors)
                maxTrustors = agents[i].trustors.size();
        }
        
        /*for(int i=0;i<communities.size();i++){
            int[] c = (int[])communities.elementAt(i);
            System.out.println((i+1)+ ":  "+users.elementAt( c[0] - 1) );
        }*/
        
        
        
        System.out.println ("Running the trust assessment code on a network with "+n+" nodes and "+m+" edges...");

    }

    
    
    
    void ratingsDiversity(int option)throws Exception{
        double[] diversities;
        switch (option){
            case (1):
                for(int i=0;i<n;i++){
                    diversities = new double[n];
                    for(int j=0;j<n;j++){
                       diversities[j] = Cosine(agents[i], agents[j]);
                    }
                    agents[i].ratingDiversities = diversities;
                }
            break;

            case (2):

            break;

            case (3):
                for(int i=0;i<n;i++){
                    diversities = new double[n];
                    for(int j=0;j<n;j++){
                       diversities[j] = L2(agents[i], agents[j]);
                    }
                    agents[i].ratingDiversities = diversities;
                }
            break;

            case (4):
                for(int i=0;i<n;i++){
                    diversities = new double[n];
                    for(int j=0;j<n;j++){
                       diversities[j] = jaccard(agents[i], agents[j]);
                    }
                    agents[i].ratingDiversities = diversities;
                }
            break;

        }
    }

    
    
    double Cosine(agent a,agent b){
        
        try{
            double dot = 0;

            for(int i=0;i<a.items.size();i++)
                for(int j=0;j<b.items.size();j++)
                if( (Integer)a.items.elementAt(i) == (Integer)b.items.elementAt(j) )
                    dot += (Double)a.ratings.elementAt(i) * (Double)b.ratings.elementAt(j);

            double sqrt_a = 0,sqrt_b = 0;

            for(int i=0;i<a.ratings.size();i++){
                sqrt_a += Math.pow( (Double)a.ratings.elementAt(i),2 );
            }

            for(int i=0;i<b.ratings.size();i++){
                sqrt_b += Math.pow( (Double)b.ratings.elementAt(i),2);
            }

            sqrt_a = Math.sqrt(sqrt_a);
            sqrt_b = Math.sqrt(sqrt_b);

            return dot/(sqrt_a * sqrt_b);
        }
        catch(ArithmeticException err){
            return 0;
        }
    }

    
    
    
    double L2(agent a,agent b){
        double sqrt = 0;
        
        for(int i=0;i<a.items.size();i++)
            for(int j=0;j<b.items.size();j++)
            if( (Integer)a.items.elementAt(i) == (Integer)b.items.elementAt(j) )
            sqrt += Math.pow( (Double)a.ratings.elementAt(i) - (Double)b.ratings.elementAt(j), 2);
        sqrt = Math.sqrt(sqrt);
        return sqrt;
    }

    
    
    
    double jaccard(agent a,agent b)throws Exception{

        try{
            int commons = 0;

            for(int i=0;i<a.items.size();i++)
                for(int j=0;j<b.items.size();j++)
                if( (Integer)a.items.elementAt(i) == (Integer)b.items.elementAt(j) )
                    commons++;
            return (double)commons / (a.items.size() + b.items.size() - commons);
        }
        catch(ArithmeticException err){
            return 0;
        }
        
    }

    
    
    
    void similarity(){
        sim1(); //Calculating similarities between agents based on neighborhood relation
        //sim2(); //Calculating similarities between agents based on Pearson correlation
    }

    
    
    
    void sim1(){
          for(int i=0;i<n;i++){
            double[] similarity = new double[n];
            for(int j=0;j<n;j++){
                if(i != j){
                    double sim = Sim_calculate(agents[i] , agents[j]);
                    if(sim == 0){
                      //  if(A[i][j] == 1)
                        if( found(agents[i].nexts, j) != -1)
                            similarity[j] = agents[i].deg * agents[j].deg * 1.0 / (4 * m);
                        else
                            similarity[j] = -agents[i].deg * agents[j].deg * 1.0 / (4 * m);
                    }
                    if(sim > 0){
                        //if(A[i][j] == 1)
                        if( found(agents[i].nexts, j) != -1)
                            similarity[j] = sim * (1 - agents[i].deg * agents[j].deg * 1.0 / (2 * m));
                        else
                            similarity[j] = sim / (n);
                    }
                }
            }
            agents[i].similarities = similarity;
        }

   }

    
    
    
    void sim2(){
        double[] mu = new double[n];
          double[] sigma = new double[n];
          int A_ij,A_ik,A_jk;

          for(int i=0;i<n;i++){
            mu[i] = agents[i].nexts.size();
            mu[i] /= n;

              for(int j=0;j<n;j++){
                  A_ij = 0;
                  if( found(agents[i].nexts, j) != -1)
                      A_ij = 1;
                  sigma[i] += Math.pow(A_ij - mu[i],2);
              }
            sigma[i] /= n;
            sigma[i] = Math.sqrt(sigma[i]);
          }
          for(int i=0;i<n;i++){
              double[] similarity = new double[n];
              for(int j=0;j<n;j++){
                  for(int k=0;k<n;k++){
                      A_ik = 0;
                      A_jk = 0;
                  if( found(agents[i].nexts, k) != -1)
                      A_ik = 1;
                  if( found(agents[j].nexts, k) != -1)
                      A_jk = 1;
                     similarity[j] += (A_ik - mu[i]) * (A_jk - mu[j]);
                  }
                     similarity[j] /= (n * sigma[i] * sigma[j]);
              }
              agents[i].similarities = similarity;
          }
    }

    
    
    
    int Sim_calculate(agent a1,agent a2){
        Vector a = a1.nexts;
        Vector b = a2.nexts;
        int similar=0;
        for(int i=0;i<a.size();i++)
            for(int j=0;j<b.size();j++){
                if((Integer)a.elementAt(i) == (Integer)b.elementAt(j))
                    similar++;
            }
        return similar;
    }

    
    
    
    
    int Game(){
        int current_agent = -1;
        iterations = 0;
        //System.out.println("\nPlease wait to get the final results...");
        do{
            iterations++;
            //if(iterations % n == 0)
            //    System.out.println("\nIteration "+iterations+" ( "+(double)iterations/n+" * n) ... "+communities.size()+" communities are detected.");

            current_agent = agentSelection(current_agent);

            //System.out.println("\nMaximum old utility for user "+ users.elementAt(current_agent) + "----> "+agents[current_agent].utilities.elementAt( max(agents[current_agent].utilities) ));
            
//            System.out.println("\nOld utility for user "+ users.elementAt(current_agent) + "----> "+agents[current_agent].utility);
            //System.out.println("Old label: "+agents[current_agent].L.lastElement());

            personalDecision(current_agent);

            //System.out.println("\nCurrent utility for user "+ users.elementAt(current_agent) + "---->"+ agents[current_agent].utilities.lastElement());
            
//            System.out.println("\nCurrent utility for user "+ users.elementAt(current_agent) + "----> "+agents[current_agent].utility);

            //System.out.println("Current label: "+agents[current_agent].L.lastElement());

            /*for(int i=0;i<communities.size();i++)
            {
                int[] c = (int[])communities.elementAt(i);
                System.out.print((i+1)+": ");
                for(int j=0;j<c.length;j++)
                    System.out.print(users.elementAt(c[j] - 1)+" ");
                System.out.println();
            }
            System.out.println("\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");


            for(int i=0;i<users.size();i++){
                System.out.print(users.elementAt(i)+": ");
                for(int j=0;j<agents[i].L.size();j++){
                    System.out.print(agents[i].L.elementAt(j)+"  ");
                }
                System.out.println();
            }
System.out.println("::::::::::::::::::::::::NEXT ITERATION::::::::::::::::::::::::::::");*/
        }while(iterations != 5 * n);

        return iterations;
    }

    
    
    
    
    void centerDetection(String outLoc) throws Exception{
        FileWriter f1;
        BufferedWriter out1;
        f1 = new FileWriter(outLoc);
        out1 = new BufferedWriter(f1);
        
        centers = new Vector();
        agent center;
        
        for(int i=0;i<communities.size();i++){
            
            int[] c = (int[])communities.elementAt(i);
            
            if ( c.length > 1 ){
                center = getImportantUser(i + 1);
                centers.addElement(center);
            }
            
            else{
                center = agents[c[0] - 1];
                centers.addElement(center);
            }
            
            
            
            for (int j=0;j<center.items.size();j++)
               out1.append(center.items.elementAt(j)+" ");
               out1.newLine();
            for (int j=0;j<center.ratings.size();j++)
               out1.append(center.ratings.elementAt(j)+" ");
               out1.newLine();
        }
        
        out1.flush();
        out1.close();
        f1.close();
    }
    

    
    
    
    int agentSelection(int current){
        int selected = current + 1;
        if (selected >= n)
            selected = 0;
        return selected;
    }

    
    
    
    
//Choose best operation among join & switch operations.
    void personalDecision(int current){

        Vector Us_Join = new Vector(); // utilities for current agent by joining next possible communities
        Vector indx_join = new Vector();

        for(int j=0;j<communities.size();j++)
        {
            if ((Integer)agents[current].num != (j + 1) && !agents[current].L.contains(j + 1)){
                agents[current].L.addElement(j + 1);
                int indx = agents[current].L.size() - 1;
                Us_Join.addElement(utility_calculate(agents[current] , method));
                indx_join.addElement(j + 1);
                agents[current].L.removeElementAt(indx);
//                System.out.println("User "+users.elementAt(current)+" wants to join community # "+(j+1)+" with utility change: "+Us_Join.lastElement());
            }
        }
        int ind_Join = max(Us_Join);

        
        
        
        
        Vector Us_Leave = new Vector(); // utilities for current agent by leaving communities
        Vector indx_leave_1 = new Vector();
        if((Integer)agents[current].L.lastElement() > 0){
            for(int i=1;i<agents[current].L.size();i++){
                int l = (Integer)agents[current].L.remove(i);
                Us_Leave.addElement(utility_calculate(agents[current],method));
                indx_leave_1.addElement(l);
//                System.out.println("User "+users.elementAt(current)+" wants to leave community # "+l + " with utility change: "+Us_Leave.lastElement());
                agents[current].L.insertElementAt(l,i);
            }
        }
        int ind_Leave = max(Us_Leave);
        
        
        
        
        Vector Us_Switch = new Vector(); // utilities for current agent by switching between communities
        Vector indx_switch = new Vector();
        Vector indx_leave = new Vector();
        if((Integer)agents[current].L.lastElement() > 0){
            for(int i=1;i<agents[current].L.size();i++){
              int l = (Integer)agents[current].L.remove(i);
              for(int j=0;j<communities.size();j++){
                //int[] c = (int[])communities.elementAt(j);
                //if (c.length >= 2){
                    if ((Integer)agents[current].num != (j + 1) && !agents[current].L.contains(j + 1) && l != j + 1){
                        agents[current].L.addElement(j + 1);
                        Us_Switch.addElement(utility_calculate(agents[current],method));
                        indx_switch.addElement(j + 1);
                        indx_leave.addElement(l);
                        agents[current].L.removeElementAt(agents[current].L.size() - 1);
//                        System.out.println("User "+users.elementAt(current)+" wants to switch from community # "+l+ " to guild #"+(j+1)+ " with utility change: "+Us_Switch.lastElement());

                    }
                //}
              }
              agents[current].L.insertElementAt(l,i);
            }
        }
        int ind_Switch = max(Us_Switch);
//******************************************************************************
//Now check whether it is better to join or leave or switch for current agent
        //int ind_max = max (agents[current].utilities);
        //double oldUtility = (Double)agents[current].utilities.elementAt(ind_max);
        double oldUtility = (Double)agents[current].utility;
//        System.out.println("OLD-UTILITY============>>>>>> "+oldUtility);
        double _join = -1,_switch = -1,_leave = -1;

        if (!Us_Join.isEmpty())
           _join = (Double)Us_Join.elementAt(ind_Join);
        if (!Us_Switch.isEmpty())
           _switch = (Double)Us_Switch.elementAt(ind_Switch);
        if (!Us_Leave.isEmpty())
           _leave = (Double)Us_Leave.elementAt(ind_Leave);
        
//        System.out.println("JOIN-UTILITY============>>>>>> "+ _join);

//        System.out.println("SWITCH-UTILITY============>>>>>> "+ _switch);
        
//        System.out.println("LEAVE-UTILITY============>>>>>> "+ _leave);
        
        
        if (oldUtility <= 0){
            if (_join <= 0 && _switch <= 0 && _leave <= 0){
                agents[current].status(3);
//               System.out.println("1. Player "+users.elementAt(current)+" did nothing!");
            }
            
            else{
                if (_join >= _switch && _join >= _leave){
                    Join(current,indx_join,ind_Join);
//                System.out.println("2. Player "+users.elementAt(current)+" joined community # "+ (Integer)indx_join.elementAt(ind_Join) ); 
                }
                
                else if (_switch > _join && _switch >= _leave){
                   Switch(current,indx_leave,indx_switch,ind_Switch);
//                System.out.println("3. Player "+users.elementAt(current)+" switched from community # "+ (Integer)indx_leave.elementAt(ind_Switch) + " to community #"+ (Integer)indx_switch.elementAt(ind_Switch) );
                }
                
                else if (_leave > _switch && _leave > _join){
                    Leave(current, indx_leave_1, ind_Leave);
//                System.out.println("4. Player "+users.elementAt(current)+" left community # "+ (Integer)indx_leave_1.elementAt(ind_Leave) );
                }
            }
        }
        
        
        
        else{
            if (oldUtility >= _join && oldUtility >= _switch && oldUtility >= _leave){
                agents[current].status(3);
//            System.out.println("5. Player "+users.elementAt(current)+" did nothing!");
            }
            
            else{
                if (_join >= _switch && _join >= _leave){
                    Join(current,indx_join,ind_Join);
//                System.out.println("6. Player "+users.elementAt(current)+" joined community # "+ (Integer)indx_join.elementAt(ind_Join) ); 
                }
                
                else if (_switch > _join && _switch >= _leave){
                   Switch(current,indx_leave,indx_switch,ind_Switch);
//                System.out.println("7. Player "+users.elementAt(current)+" switched from community # "+ (Integer)indx_leave.elementAt(ind_Switch) + " to community #"+ (Integer)indx_switch.elementAt(ind_Switch) );
                }
                
                else if (_leave > _switch && _leave > _join){
                    Leave(current, indx_leave_1, ind_Leave);
//                System.out.println("8. Player "+users.elementAt(current)+" left community # "+ (Integer)indx_leave_1.elementAt(ind_Leave) );
                }
            }
        }

        
    }

    
    
    
    int found(Vector v,Object item){
        for(int i=0;i<v.size();i++)
            if(v.elementAt(i).equals(item))
                return i;
        return -1;
    }

    
    
    
    double utility_calculate(agent a_current,int method){
        double Q=0;
        int current = a_current.num - 1;
        int a_current_label;
        
        for(int i=1;i<a_current.L.size();i++){
            
            a_current_label = (Integer)a_current.L.elementAt(i);
            
            int[] members = (int[])communities.elementAt(Math.abs( a_current_label ) - 1);

            for(int j=0;j<members.length;j++){
                if (method == 1)
                    Q += agents[current].similarities[members[j]-1];
                else if (method == 2)
                    Q += (agents[current].ratingDiversities[members[j]-1]/m);
                else if (method == 3)
                    Q += 1/(m * agents[current].ratingDiversities[members[j]-1]);
                 else if (method == 4)
                    Q -= (agents[current].ratingDiversities[members[j]-1]/m);
                else if (method == 5)
                    Q += alpha * agents[current].similarities[members[j]-1] + (1 - alpha) * (agents[current].ratingDiversities[members[j]-1]/m);
                else if (method == 6)
                    Q += alpha * agents[current].similarities[members[j]-1] + (1 - alpha) / (m * agents[current].ratingDiversities[members[j]-1]);
                else if (method == 7)
                    Q += alpha * agents[current].similarities[members[j]-1] - (1 - alpha) * (agents[current].ratingDiversities[members[j]-1]/m);
            }
            
        }
        
        Q -= (agents[current].L.size() - 1) / m;
        
        return Q;
    }

    
    
    
    
    int max(Vector v){
        int ind = 0;
        for(int i=1;i<v.size();i++)
            if((Double)v.elementAt(i)>(Double)v.elementAt(ind))
                ind = i;
        return ind;
    }

    
    
    
    
    double Join(int current,Vector indx_join,int ind_Join){
       agents[current].status(1);
       
       int[] temp1 = (int[])communities.elementAt((Integer)indx_join.elementAt(ind_Join) - 1);
       int[] temp = new int[temp1.length+1];
       for(int i=0;i<temp1.length;i++)
           temp[i]=temp1[i];
       temp[temp.length-1]=current+1;

       agents[current].L.addElement( (Integer)indx_join.elementAt(ind_Join) );
       //agents[current].utilities.addElement( utility_calculate(agents[current],method) );
       agents[current].utility = utility_calculate(agents[current],method);
       
       communities.insertElementAt(sort(temp),(Integer)indx_join.elementAt(ind_Join) - 1);
       communities.removeElementAt((Integer)indx_join.elementAt(ind_Join));
       

       for(int i=0;i<temp.length;i++){
           if(temp[i] != current + 1){
               int ind = found(agents[temp[i]-1].L, (Integer)indx_join.elementAt(ind_Join));
               
               if ( ind == -1 ){
                    agents[temp[i]-1].L.add( (Integer)indx_join.elementAt(ind_Join) );
                    //agents[temp[i]-1].utilities.addElement( utility_calculate(agents[temp[i]-1],method) );
                    
               }
               
               agents[temp[i]-1].utility = utility_calculate(agents[temp[i]-1],method);
               
               //else{
               //    agents[temp[i]-1].utilities.insertElementAt(utility_calculate(agents[temp[i]-1],method) , ind);
               //    agents[temp[i]-1].utilities.removeElementAt(ind + 1);
               //}
           }
        }
       
        //return (Double)agents[current].utilities.lastElement();
       return agents[current].utility;
    }

    
    
    
    double Switch(int current,Vector indx_leave,Vector indx_switch,int ind_Switch){
            agents[current].status(2);

            int[] temp = (int[])communities.elementAt((Integer)indx_leave.elementAt(ind_Switch) - 1);

            int ind_removed = find_index(temp,agents[current].num);

            int[] temp2 = new int[temp.length-1];

            int count = 0;
            
            for(int i=0;i<temp.length;i++){
                if(i != ind_removed){
                    temp2[count] = temp[i];
                    count++;
                }
            }

            //Leave
            communities.insertElementAt(sort(temp2),(Integer)indx_leave.elementAt(ind_Switch) - 1);
            communities.removeElementAt( (Integer)indx_leave.elementAt(ind_Switch) );
            int ind = found(agents[current].L, (Integer)indx_leave.elementAt(ind_Switch));
            agents[current].L.removeElementAt( ind );
            //agents[current].utilities.removeElementAt( ind);
            agents[current].utility = utility_calculate(agents[current],method);
            
            
            if(temp2.length > 1){
                for(int i=0;i<temp2.length;i++){
                    ind = found(agents[temp2[i]-1].L, (Integer)indx_leave.elementAt(ind_Switch));
                    //agents[temp2[i]-1].utilities.add(ind, utility_calculate(agents[temp2[i]-1],method));
                    //agents[temp2[i]-1].utilities.removeElementAt(ind + 1);
                    agents[temp2[i]-1].utility = utility_calculate(agents[temp2[i]-1],method);
                }
            }
            
            else{
                ind = found(agents[temp2[0]-1].L, (Integer)indx_leave.elementAt(ind_Switch));
                agents[temp2[0]-1].L.removeElementAt(ind);
                //agents[temp2[0]-1].utilities.removeElementAt(ind);
                
                agents[temp2[0]-1].utility = utility_calculate(agents[temp2[0]-1],method);
            }

            
            
           //Join
           int[] temp1 = (int[])communities.elementAt((Integer)indx_switch.elementAt(ind_Switch) - 1);
           temp = new int[temp1.length+1];
           for(int i=0;i<temp1.length;i++)
              temp[i]=temp1[i];
           temp[temp.length-1]=current+1;

           agents[current].L.addElement((Integer)indx_switch.elementAt(ind_Switch) );
           //agents[current].utilities.addElement( utility_calculate(agents[current], method) );
            agents[current].utility = utility_calculate(agents[current], method);
           
           
           communities.insertElementAt(sort(temp),(Integer)indx_switch.elementAt(ind_Switch) - 1);
           communities.removeElementAt((Integer)indx_switch.elementAt(ind_Switch));

           for(int i=0;i<temp.length;i++)
             if(temp[i] != current + 1){
                 ind = found(agents[temp[i]-1].L, (Integer)indx_switch.elementAt(ind_Switch));
                 
                 if (ind != -1){
                    //agents[temp[i]-1].utilities.add(ind, utility_calculate(agents[temp[i]-1],method));
                    //agents[temp[i]-1].utilities.removeElementAt(ind + 1);
                 }
                 
                 else{
                     agents[temp[i]-1].L.addElement((Integer)indx_switch.elementAt(ind_Switch));
                     //agents[temp[i]-1].utilities.addElement( utility_calculate(agents[temp[i]-1],method) );
                 }
                 
                 agents[temp[i]-1].utility = utility_calculate(agents[temp[i]-1],method);
             }


        //return (Double)agents[current].utilities.lastElement();
            return agents[current].utility;
    }

    
    
    
    
    double Leave(int current,Vector indx_leave,int ind_Leave){
            agents[current].status(4);

            int[] temp = (int[])communities.elementAt((Integer)indx_leave.elementAt(ind_Leave) - 1);

            int ind_removed = find_index(temp,agents[current].num);

            int[] temp2 = new int[temp.length-1];

            int count = 0;
            
            for(int i=0;i<temp.length;i++){
                if(i != ind_removed){
                    temp2[count] = temp[i];
                    count++;
                }
            }

            //Leave
            communities.insertElementAt(sort(temp2),(Integer)indx_leave.elementAt(ind_Leave) - 1);
            communities.removeElementAt( (Integer)indx_leave.elementAt(ind_Leave) );
            int ind = found(agents[current].L, (Integer)indx_leave.elementAt(ind_Leave));
            agents[current].L.removeElementAt( ind );
            //agents[current].utilities.removeElementAt( ind);
            agents[current].utility = utility_calculate(agents[current], method);
            
            
            
            if(temp2.length > 1){
                for(int i=0;i<temp2.length;i++){
                    ind = found(agents[temp2[i]-1].L, (Integer)indx_leave.elementAt(ind_Leave));
                    //agents[temp2[i]-1].utilities.add(ind, utility_calculate(agents[temp2[i]-1],method));
                    //agents[temp2[i]-1].utilities.removeElementAt(ind + 1);
                    
                    agents[temp2[i]-1].utility = utility_calculate(agents[temp2[i]-1],method);
                 }
            }

            else{
                ind = found(agents[temp2[0]-1].L, (Integer)indx_leave.elementAt(ind_Leave));
                agents[temp2[0]-1].L.removeElementAt(ind);
                //agents[temp2[0]-1].utilities.removeElementAt(ind);
                
                agents[temp2[0]-1].utility = utility_calculate(agents[temp2[0]-1],method);
            }
            

        //return (Double)agents[current].utilities.lastElement();
            
            return agents[current].utility;
    }

    
    
    
    int find_index(int[] a,int b){
        for(int i=0;i<a.length;i++)
            if(a[i] == b)
                return i;
        return -1;
    }

    
    
    
    int[] sort(int[] a){
        int temp=0;
        for(int i=0;i<a.length-1;i++){
            for(int j=i+1;j<a.length;j++){
                if(a[i]>a[j]){
                    temp = a[i];
                    a[i] = a[j];
                    a[j] = temp;
                }
            }
        }
        return a;
    }

    
    
    
    void printCommunities(String outLoc) throws Exception{
        FileWriter f1;
        BufferedWriter out1;
        f1 = new FileWriter(outLoc);
        out1 = new BufferedWriter(f1);
        //System.out.println("guild Size ----> "+communities.size());
        for(int i=0;i<communities.size();i++){
            int[] temp = (int[])communities.elementAt(i);
            //System.out.println("Size of community #"+(i+1)+" is : "+temp.length);
            for(int j=0;j<temp.length;j++){
               out1.append(users.elementAt(temp[j] - 1)+" ");
            }
            if (i < communities.size() - 1)
               out1.newLine();
        }
        out1.flush();
        out1.close();
        f1.close();
    }





    double accuracyCalculate(double x, String outLoc)throws Exception{
        double PA = 0;
        
        System.out.println("STARTING ACCURACY");
        FileWriter f1;
        BufferedWriter out1;
        f1 = new FileWriter(outLoc+"");
        out1 = new BufferedWriter(f1);

        String[] trust = new String[3];
        N = new Vector();
        //BUN = new Vector();
        String[] temp;
        
        //System.out.println("HEREEEEEEEEEE: "+(int)Math.round(x*trusts.size()/100));
        

        
        for(int i = (int)Math.round(x*trusts.size()/100); i<trusts.size() ; i++){
            temp = (String[])trusts.elementAt(i);
            //System.out.println(temp[0]+"\t"+temp[1]+"\t"+temp[2]);
            trust = new String[3];
            trust[0] = temp[0];
            trust[1] = temp[1];
            //System.out.println("Before prediction...");
            trust[2] = predict(trust[0],trust[1])+"";
            //System.out.println((i+1)+"\t"+trust[0]+"\t"+trust[1]+"\t"+trust[2]);
            N.addElement( trust );
//            BUN.addElement( trust );
            //System.out.println("**********************************************");
        }
        System.out.println("N completed");
        
        
        
        
//        int count;
//        for(int i=0;i<users.size();i++){
//            for(int j=0;j<users.size();j++){
//                if (i != j){
//                    count = 0;
//                    trust = new String[3];
//                    trust[0] = (String)users.elementAt(i);
//                    trust[1] = (String)users.elementAt(j);
//                    for(int k=0;k<trusts.size();k++){
//                        temp = (String[])trusts.elementAt(k);
//                        if (trust[0].equals(temp[0])  &&   trust[1].equals(temp[1])){
//                            count ++;
//                            break;
//                        }
//                    }
//                    
//                    if (count == 0){
//                        trust[2] = predict(trust[0],trust[1])+"";    
//                        //System.out.println(trust[0]+"\t"+trust[1]+"\t"+trust[2]);
//                        //System.out.println("**********************************************");
//                        BUN.addElement(trust);
//                    }
//                    
//                }
//            }
//        }
//        System.out.println("B U N completed");
        
        
        

//        double[][] ranked = new double[BUN.size()][2];
        double[][] ranked = new double[N.size()][2];
        
        for(int i=0;i<ranked.length;i++){
//            trust = (String[])BUN.elementAt(i);
            trust = (String[])N.elementAt(i);
            ranked[i][0] = i;
            ranked[i][1] = Double.parseDouble(trust[2]);
        }
        //System.out.println("Ranking got started");
        
        
        double temmp,temmmp;

        for(int i=0;i<ranked.length-1;i++){
            for(int j=i+1;j<ranked.length;j++){

                if (ranked[i][1] < ranked[j][1]){
                    temmp = ranked[i][0];
                    temmmp = ranked[i][1];
                    
                    ranked[i][0] = ranked[j][0];
                    ranked[i][1] = ranked[j][1];
                    
                    ranked[j][0] = temmp;
                    ranked[j][1] = temmmp;
                }
            
            
            }
        }
        //System.out.println("Sorting is completed");
        
        
//        Vector final_BUN = new Vector();
//       
//        for(int i=0;i<ranked.length;i++){
//            final_BUN.addElement( BUN.elementAt( (int)ranked[i][0] ) );
//        }
        
        
//        
        Vector final_N = new Vector();
        
        for(int i=0;i<ranked.length;i++){
            final_N.addElement( N.elementAt( (int)ranked[i][0] ) );
        }
        
        //System.out.println("Now constructing the final sorted vector");
        
        
//        for(int i=0;i<final_BUN.size();i++){
//            trust = new String[3];
//            trust = (String[])final_BUN.elementAt(i);
//            out1.append(trust[0]+"\t"+trust[1]+"\t"+trust[2]);
//            out1.newLine();
//        }
        
//        
        for(int i=0;i<final_N.size();i++){
            trust = new String[3];
            trust = (String[])final_N.elementAt(i);
            out1.append(trust[0]+"\t"+trust[1]+"\t"+trust[2]);
            out1.newLine();
        }
        
        
        //System.out.println("Appending to the file");
        
        
        out1.flush();
        out1.close();
        f1.close();
        
        
//        int _Nc = 0;
//        String[] trust1,trust2;
//        for(int i=0;i<final_BUN.size();i++){
//            trust1 = (String[])final_BUN.elementAt(i);
//            for(int j=0;j<N.size();j++){
//                trust2 = (String[])N.elementAt(j);
//                if (trust1[0].equals( trust2[0] ) && trust1[1].equals( trust2[1] ) && !trust1[2].equals("NaN") && Double.parseDouble(trust1[2]) > 0.5){
//                    _Nc++;
//                    break;
//                }
//            }
//        }
        
        
        int _Nc = 0;
        String[] trust1,trust2;
        for(int i=0;i<final_N.size();i++){
            trust1 = (String[])final_N.elementAt(i);
            for(int j=0;j<N.size();j++){
                trust2 = (String[])N.elementAt(j);
                if (trust1[0].equals( trust2[0] ) && trust1[1].equals( trust2[1] ) && !trust1[2].equals("NaN") && Double.parseDouble(trust1[2]) > 0.5){
                    _Nc++;
                    break;
                }
            }
        }
        
        
        //System.out.println("Calculating final accuracy");
        
        
        if (!N.isEmpty())
            PA = (double)_Nc / N.size();
        
        return PA;
    }

    
    
    
    
    double accuracyCalculate2(double x, double y, String outLoc) throws Exception{
        double PA = 0;
        
        System.out.println("@@@@@@@STARTING ACCURACY########2");
        FileWriter f1;
        BufferedWriter out1;
        f1 = new FileWriter(outLoc+"");
        out1 = new BufferedWriter(f1);
        
        String[] trust = new String[3];
        Vector newN = new Vector();
//        Vector newBUN = new Vector();

        int l1 = trusts.size() - (int)Math.round(x*trusts.size()/100);
        int l2 = trusts.size() - (int)Math.round(y*trusts.size()/100);
        for(int i = l1-l2; i<N.size() ; i++){
            newN.addElement(N.elementAt(i));
        }
        
//        for(int i=l1-l2;i<BUN.size();i++){
//            newBUN.addElement(BUN.elementAt(i));
//        }
        
        
//        double[][] ranked = new double[newBUN.size()][2];
        double[][] ranked = new double[newN.size()][2];
        
        for(int i=0;i<ranked.length;i++){
//            trust = (String[])newBUN.elementAt(i);
            trust = (String[])newN.elementAt(i);
            ranked[i][0] = i;
            ranked[i][1] = Double.parseDouble(trust[2]);
        }
        
        
        double temmp,temmmp;

        for(int i=0;i<ranked.length-1;i++){
            for(int j=i+1;j<ranked.length;j++){

                if (ranked[i][1] < ranked[j][1]){
                    temmp = ranked[i][0];
                    temmmp = ranked[i][1];
                    
                    ranked[i][0] = ranked[j][0];
                    ranked[i][1] = ranked[j][1];
                    
                    ranked[j][0] = temmp;
                    ranked[j][1] = temmmp;
                }
            
            
            }
        }
        
        
//        Vector final_BUN = new Vector();
        Vector final_N = new Vector();
        for(int i=0;i<ranked.length;i++){
//            final_BUN.addElement( newBUN.elementAt( (int)ranked[i][0] ) );
            final_N.addElement( newN.elementAt( (int)ranked[i][0] ) );
        }
        
        
        
//        for(int i=0;i<final_BUN.size();i++){
//            trust = new String[3];
//            trust = (String[])final_BUN.elementAt(i);
//            out1.append(trust[0]+"\t"+trust[1]+"\t"+trust[2]);
//            out1.newLine();
//        }
        
        
        for(int i=0;i<final_N.size();i++){
            trust = new String[3];
            trust = (String[])final_N.elementAt(i);
            out1.append(trust[0]+"\t"+trust[1]+"\t"+trust[2]);
            out1.newLine();
        }
//        
        

        out1.flush();
        out1.close();
        f1.close();
        
//        
//        int _Nc = 0;
//        String[] trust1,trust2;
//        for(int i=0;i<final_BUN.size();i++){
//            trust1 = (String[])final_BUN.elementAt(i);
//            for(int j=0;j<newN.size();j++){
//                trust2 = (String[])newN.elementAt(j);
//                if (trust1[0].equals( trust2[0] ) && trust1[1].equals( trust2[1] ) && !trust1[2].equals("NaN") && Double.parseDouble(trust1[2]) > 0.5){
//                    _Nc++;
//                    break;
//                }
//            }
//        }
//        
        
        int _Nc = 0;
        String[] trust1,trust2;
        for(int i=0;i<final_N.size();i++){
            trust1 = (String[])final_N.elementAt(i);
            for(int j=0;j<N.size();j++){
                trust2 = (String[])N.elementAt(j);
                if (trust1[0].equals( trust2[0] ) && trust1[1].equals( trust2[1] ) && !trust1[2].equals("NaN") && Double.parseDouble(trust1[2]) > 0.5){
                    _Nc++;
                    break;
                }
            }
        }
//        
        
        
        if (!newN.isEmpty())
            PA = (double)_Nc / newN.size();
        
        
        return PA;
    }

    
    
    

    void printAccuracy(String outLoc,double PA) throws Exception{
        FileWriter f1;
        BufferedWriter out1;
        f1 = new FileWriter(outLoc);
        out1 = new BufferedWriter(f1);
        
        out1.append("Accuracy: "+PA);
        
        out1.flush();
        out1.close();
        f1.close();
    }
    
    


    
     
    double predict(String a, String b)throws Exception{
        //System.out.println("In predict function...");
        int ind1 = found (users, a);
        int ind2 = found (users, b);
        
        Vector predictions = new Vector();
        
        for (int i=0;i<agents[ind1].L.size();i++){
            
            int l1 = (Integer)agents[ind1].L.elementAt(i);
            agent center1 = (agent)centers.elementAt(Math.abs(l1) - 1);
                
            for (int j=0;j<agents[ind2].L.size();j++){
                    int l2 = (Integer)agents[ind2].L.elementAt(j);
                    agent center2 = (agent)centers.elementAt(Math.abs(l2) - 1);
                    
                    //System.out.println("Labels: "+l1+",,,,,,,"+l2); 
                     
                     
                    double dist1=0,dist2=0,dist3=0;

                    if (centrality != 1){ //Betweenness and Eigenvector centralities
                        dist1 = agents[ind1].ratingDiversities[center1.num - 1];
                        dist2 = agents[ind2].ratingDiversities[center2.num - 1];
                        dist3 = agents[center1.num - 1].ratingDiversities[center2.num - 1];
                    }


                    else{ //Average centrality
                        if (selectedOption == 1) { //cosine
                            dist1 = Cosine (agents[ind1],center1);
                            dist2 = Cosine (agents[ind2],center2);
                            dist3 = Cosine (center1,center2);
                        }

                        else if (selectedOption ==2) { //

                        }

                        else if (selectedOption == 3) { //L2
                            dist1 = L2 (agents[ind1],center1);
                            dist2 = L2 (agents[ind2],center2);
                            dist3 = L2 (center1,center2);
                        }

                        else if (selectedOption == 4) { //Jaccard
                            dist1 = jaccard (agents[ind1],center1);
                            dist2 = jaccard (agents[ind2],center2);
                            dist3 = jaccard (center1,center2);
                        }
                    }


                    //System.out.println("Dist1---> "+dist1);
                    //System.out.println("Dist2---> "+dist2);
                    //System.out.println("Dist3---> "+dist3);
                    
                    
                    if (combination == 1)
                        predictions.addElement( (dist1 + dist2 + dist3) / 3 );
        
                    else
                        predictions.addElement(dist1 * dist2 * dist3 );
                     
                }
        }
        
        int ind_max = max(predictions);
        return (Double)predictions.elementAt(ind_max);
    }



    
    
    agent getImportantUser(int label){
       if (centrality == 1){
           return avgCentrality(label);
       }
       
       
       else if (centrality == 2){
            return betweennessCentrality(label);
       }


       else if (centrality == 3){
           return eigenvectorCentrality(label);
       }
       
       else if (centrality == 4){
           return maxtrustorCentrality(label);
       }
       
        else if (centrality == 5){
           return maxtrusteeCentrality(label);
       }
       
       
       else if (centrality == 6){
           return maxdegreeCentrality(label);
       }
       
       
       else if (centrality == 7){
           return randomCentrality(label);
       }
       
       
       
       else{
           return null;
       }
    }



    

    agent betweennessCentrality(int label){
        Graph<Integer, Integer> g = new SparseMultigraph<Integer, Integer>();

        int[] community = (int[])communities.elementAt(label - 1);
        
        //System.out.println("In betweenness function");
        
        //for(int i=0;i<community.length;i++)
            //System.out.print(users.elementAt( community[i] - 1)+" ");
        
        //System.out.println();
        
        
        
        for(int i=0;i<community.length;i++)
            g.addVertex(agents[community[i]-1].num - 1);

        int e = 0;
        for(int i=0;i<community.length;i++)
            for(int j=0;j<agents[community[i]-1].deg;j++){
                   g.addEdge(e,agents[community[i]-1].num-1,(Integer)agents[community[i]-1].nexts.elementAt(j));
                   e++;
            }

        //System.out.println("Graph:");
        
        //System.out.println("---------------------------------------------------");
        BetweennessCentrality b = new BetweennessCentrality(g);
        b.setRemoveRankScoresOnFinalize(false);
        b.evaluate();

        double[] ranks = new double[community.length];

        for(int i=0;i<community.length;i++){
            ranks[i] = b.getVertexRankScore(agents[community[i]-1].num - 1);
            //System.out.println("Rank "+ranks[i]);
        }

        
        
        
        int max = 0;
        for(int i=1;i<ranks.length;i++)
            if (ranks[i] > ranks[max])
                max = i;

        return agents[community[max]-1];
    }
    





    agent eigenvectorCentrality(int label){
        Graph<Integer, Integer> g = new SparseMultigraph<Integer, Integer>();

        int[] community = (int[])communities.elementAt(label - 1);

        for(int i=0;i<community.length;i++)
            g.addVertex(agents[community[i]-1].num - 1);

        int e = 0;
        for(int i=0;i<community.length;i++)
            for(int j=0;j<agents[community[i]-1].deg;j++){
                   g.addEdge(e,agents[community[i]-1].num-1,(Integer)agents[community[i]-1].nexts.elementAt(j));
                   e++;
            }

        EigenvectorCentrality eg = new EigenvectorCentrality(g);

        double[] ranks = new double[community.length];

        for(int i=0;i<community.length;i++){
            ranks[i] = Double.parseDouble( eg.getVertexScore(agents[community[i]-1].num - 1).toString() );
        }

        int max = 0;
        for(int i=1;i<ranks.length;i++)
            if (ranks[i] > ranks[max])
                max = i;

        return agents[community[max]-1];
    }





    agent avgCentrality(int label){
        
        int[] community = (int[])communities.elementAt(label - 1);

        agent avg = new agent(-1);
        avg.setL( new Vector() );

        for(int i=0;i<community.length;i++){
            for(int j=0;j<agents[community[i] - 1].items.size();j++){
                if (!avg.items.contains( agents[community[i] - 1].items.elementAt(j) )){
                    avg.items.addElement( agents[community[i] - 1].items.elementAt(j) );
                }
            }
        }

        int average,ind,num;
        for(int i=0;i<avg.items.size();i++){
            average = 0; num = 0;
            for(int j=0;j<community.length;j++){
                ind = found (agents[community[j] - 1].items,avg.items.elementAt(i));
                if ( ind != -1 ){
                  average += (Double)agents[community[j] - 1].ratings.elementAt(ind);
                  num++;
                }
            }
            avg.ratings.addElement( (double)average/num );
        }

        
        return avg;
    }
    
    
    
    
    agent maxtrustorCentrality(int label){
        
        int[] community = (int[])communities.elementAt(label - 1);

        for(int i=0;i<community.length;i++){
            for(int j=0;j<community.length;j++){
                if (j != i && agents[community[i] - 1].trustors.contains(agents[community[j] - 1].num))
                    agents[community[i] - 1].comm_trustors.addElement(agents[community[j] - 1].num);
            }
        }
        
        
        int max = 0;
        for(int i=1;i<community.length;i++)
            if (agents[community[i] - 1].comm_trustors.size() > agents[community[max] - 1].comm_trustors.size())
                max = i;

        return agents[community[max]-1];
    }
    
    
    
    
    
    
    
    
    agent maxtrusteeCentrality(int label){
        int[] community = (int[])communities.elementAt(label - 1);

        for(int i=0;i<community.length;i++){
            for(int j=0;j<community.length;j++){
                if (j != i && agents[community[i] - 1].trustees.contains(agents[community[j] - 1].num))
                    agents[community[i] - 1].comm_trustees.addElement(agents[community[j] - 1].num);
            }
        }
        
        
        int max = 0;
        for(int i=1;i<community.length;i++)
            if (agents[community[i] - 1].comm_trustees.size() > agents[community[max] - 1].comm_trustees.size())
                max = i;

        return agents[community[max]-1];
    }
    
    
    
    
    
    
    
    agent maxdegreeCentrality(int label){
        int[] community = (int[])communities.elementAt(label - 1);

        for(int i=0;i<community.length;i++){
            for(int j=0;j<community.length;j++){
                if (j != i && agents[community[i] - 1].nexts.contains(agents[community[j] - 1].num))
                    agents[community[i] - 1].comm_deg++;
            }
        }
        
        int max = 0;
        for(int i=1;i<community.length;i++)
            if (agents[community[i] - 1].comm_deg > agents[community[max] - 1].comm_deg)
                max = i;

        return agents[community[max]-1];
    }
    
    
    
    
    
    
    
    agent randomCentrality(int label){
        int[] community = (int[])communities.elementAt(label - 1);
        
        Random r = new Random();
        
        return agents[community[r.nextInt(community.length)] - 1];
    }
    

}



class agent{
    int num = 0;//Number
    int deg = 0;
    int comm_deg = 0;
    int joined = 0;
    int switched = 0;
    int left = 0;
    int noOp = 0;
    int invited = 0;
    double utility = 0;
    Vector nexts = new Vector();
    double[] similarities;
    double[] ratingDiversities;
    Vector L = new Vector(); //Labels
    //Vector utilities = new Vector();
    Vector ratings = new Vector();
    Vector items = new Vector();
    Vector trustors = new Vector();
    Vector trustees = new Vector();
    Vector comm_trustors = new Vector();
    Vector comm_trustees = new Vector();
    
    agent(int num){
        this.num = num;
    }

    void setL(Vector L){
        for(int i=0;i<L.size();i++)
            this.L.addElement(L.elementAt(i));
    }


    void status(int status){
        switch(status){
            case 1:
                joined++;
                break;
            case 2:
                switched++;
                break;
            case 3:
                noOp++;
                break;
            case 4:
                left++;
                break;
            default:
                break;
        }
    }
}
//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
//End of all classes