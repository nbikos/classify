package classification;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 *
 * @author nick
 */
public class NaiveBayes{
    static ArrayList<int[]> TestData = new ArrayList<int[]>();
    //static ArrayList<Boolean> resultIn = new ArrayList<Boolean>();
    static ArrayList<int[]> liveList = new ArrayList<int[]>();
    static ArrayList<int[]> dieList = new ArrayList<int[]>();
    static ArrayList<int[]> poss = new ArrayList<int[]>();
    static int samples = 0;
    static int attrib = 0;
    static int trueneg = 0;
    static int truepos = 0;
    static int falseneg = 0;
    static int falsepos = 0;
    static ArrayList<Boolean> testIn = new ArrayList<Boolean>();
    static int[] sizes;
    
    

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        int count = 0;
        if (args.length != 2){
               System.err.println("$ java Classify Train.txt Test.txt");
               System.exit(1);
        }
           ReadTraining(args[0]);
           ReadTesting(args[1]);
           sizes = new int[attrib];
           while(count < attrib)
           {
               sizes[count] = samples;
               ++count;
           }
           AllPossible();
           
           Laplace();
 
           RunTest();
           //create list of all training data
           //list will always be saved
           //create list of weights for each record of training data
           //create CDF using weights
           //use CDF to fill new training set
           //use new training set, use list to record hits and misses
           //hits are penalized, misses remain, in new weighted list
           //normalize all weights by multiplying sumofold/sumofnew
           //oldweights are now useless
           //use weights to calculate error
           //throw away errors over .5
           
           //RunTesting(args[1]);
           System.out.println(truepos);
           System.out.println(falseneg);
           System.out.println(falsepos);
           System.out.println(trueneg);
           
           double Truepos = (double)truepos;
           double Trueneg = (double)trueneg;
           double Falsepos = (double)falsepos;
           double Falseneg= (double)falseneg;
           System.out.print("accuracy: ");
           System.out.println((Truepos+Trueneg)/(Truepos+Trueneg+Falsepos+Falseneg));
           System.out.print("error rate: ");
           System.out.println(1-((Truepos+Trueneg)/(Truepos+Trueneg+Falsepos+Falseneg)));
           System.out.print("sensitivity: ");
           System.out.println(Truepos/(Truepos+Falseneg));
           System.out.print("specificity: ");
           System.out.println(Trueneg/(Trueneg+Falsepos));
           System.out.print("precision: ");
           System.out.println(Truepos/(Truepos+Falsepos));
           System.out.print("F score: ");
           System.out.println((2*(Truepos/(Truepos+Falsepos))*(Truepos/(Truepos+Falseneg)))/((Truepos/(Truepos+Falsepos))+(Truepos/(Truepos+Falseneg))));
           System.out.print("F/beta 0.5: ");
           System.out.println(((1+.5*.5)*(Truepos/(Truepos+Falsepos))*(Truepos/(Truepos+Falseneg)))/((.5*.5)*(Truepos/(Truepos+Falsepos))+(Truepos/(Truepos+Falseneg))));
           System.out.print("F/beta 2: ");
           System.out.println(((1+2*2)*(Truepos/(Truepos+Falsepos))*(Truepos/(Truepos+Falseneg)))/((2*2)*(Truepos/(Truepos+Falsepos))+(Truepos/(Truepos+Falseneg))));
           
           
           //will line by line decide prediction
           //search all lives of each attrib for selected
           //search all dies
           //will load giant prediction list
           //will compare lists and make counts
           //print results
           System.exit(0);
           
        
    }
   
   private static void RunTest()
   {
       int testIt = 0;
       int trIt = 0;
       int count = 0;
       int occurredL = 0;
       int occurredD = 0;
       boolean result = true;
       boolean guess = true;
       double plive = 1.0;
       double pdie = 1.0;
       while(testIt < testIn.size())
       {
           result = testIn.get(testIt);
           count = 0;
           while(count < attrib)
           {    
                occurredD = 0;
                occurredL = 0;
                trIt = 0;
                while(trIt < liveList.size())
                {
                      if(liveList.get(trIt)[count] == TestData.get(testIt)[count])
                        {
                             ++occurredL;
                        }
                        ++trIt;
                }
                trIt = 0;
                while(trIt < dieList.size())
                {
                    if(dieList.get(trIt)[count] == TestData.get(testIt)[count])
                        {
                             ++occurredD;
                        }
                    ++trIt;
                }
                if(occurredL == 0)
                {
                    plive = (double)1/(double)sizes[count]*plive;
                }
                else
                {
                    plive = (double)occurredL/(double)sizes[count]*plive;
                }
                if(occurredD == 0)
                {
                    pdie = (double)1/(double)sizes[count]*pdie;
                }
                else
                {
                    pdie = (double)occurredD/(double)sizes[count]*pdie;
                }
                ++count;
            }
           if(liveList.size() == 0)
           {
               plive = (double)1/(double)(samples+1)*plive;
           }
           else
           {
               plive = (double)liveList.size()/(double)samples*plive;
           }
           if(dieList.size() == 0)
           {
               pdie = (double)1/(double)(samples+1)*pdie;
           }
           else
           {
               pdie = (double)dieList.size()/(double)samples*pdie;
           }
           if(plive>=pdie)
               guess = true;
           else
               guess = false;
           if(guess == result)
           {
               if(guess == true)
                   ++truepos;//hit
               else
                   ++trueneg;
           }
           else
           {
               if(guess == false)
                   ++falseneg;//miss
               else
                   ++falsepos;
           }
           plive = 1.0;
           pdie = 1.0;
           ++testIt;
       }
   }
   private static void Laplace()
   {
       int trIt = 0;
       int possIt = 0;
       int count = 0;
       int occurredD = 0;
       int occurredL = 0;
       while(count < attrib)
       {
           possIt = 0;
           while(possIt < poss.get(count).length)
           {
               trIt = 0; 
               while(trIt < liveList.size())
                {
                    if(liveList.get(trIt)[count] == poss.get(count)[possIt])
                    {
                         ++occurredL;
                    }
                    ++trIt;
               }
               if(occurredL == 0)
               {
                   ++sizes[count];
               }
               occurredL = 0;
               ++possIt;
           }
           possIt = 0;
           while(possIt < poss.get(count).length)
           {
               trIt = 0; 
               while(trIt < dieList.size())
                {
                    if(dieList.get(trIt)[count] == poss.get(count)[possIt])
                    {
                         ++occurredD;
                    }
                    ++trIt;
               }
               if(occurredD == 0)
               {
                   ++sizes[count];
               }
               occurredD = 0;
               ++possIt;
           }
           
           ++count;
       }
   }
    private static void AllPossible()
   {
       int count = 0;
       int counter = 0;
       int dataIt = 0;
       int[] possarr;
       List<Integer> possin = new ArrayList<Integer>();
       while(count < attrib)
       {
           dataIt = 0;
           while(dataIt < liveList.size())
           {
               if(!(possin.isEmpty()))
               {
                    if(possin.contains(liveList.get(dataIt)[count]))
                    {
                        //skip
                    }
                    else
                    {
                        possin.add(liveList.get(dataIt)[count]);
                    }
                }
               else
               {
                   possin.add(liveList.get(dataIt)[count]);
               }
               ++dataIt;
            }
           dataIt = 0;
           while(dataIt < dieList.size())
           {
               if(!(possin.isEmpty()))
               {
                    if(possin.contains(dieList.get(dataIt)[count]))
                    {
                        //skip
                    }
                    else
                    {
                        possin.add(dieList.get(dataIt)[count]);
                    }
                }
               else
               {
                   possin.add(dieList.get(dataIt)[count]);
               }
               ++dataIt;
           }
           dataIt = 0;
           while(dataIt < TestData.size())
           {
               if(!(possin.isEmpty()))
               {
                    if(possin.contains(TestData.get(dataIt)[count]))
                    {
                        //skip
                    }
                    else
                    {
                        possin.add(TestData.get(dataIt)[count]);
                    }
                }
               else
               {
                   possin.add(TestData.get(dataIt)[count]);
               }
               ++dataIt;
           }
           //poss now holds all possible values of the attribue "count"
           possarr = new int[possin.size()];
           counter = 0;
           while(counter < possin.size())
           {
               possarr[counter] = possin.get(counter);
               ++counter;
           }
           //possarr is now an integer array of all possibilities
           poss.add(possarr);
           //attribute "count"'s possible values now in poss.get(count)
          ++count;
       }
       
   }
    private static void ReadTraining(String file){
        try {
            int attribs = 0;
            int count;
            char charholder = 0;
            FileInputStream fstream = new FileInputStream(file);
            //file has been loaded to fstream
            BufferedReader br = new BufferedReader(new InputStreamReader(new DataInputStream(fstream)));
            //buffered reader is ready
            String strLine = "";
            //initialized
            String[] splitLine;
            int[] myarr;
            //debugging vars
            //int test =0;
            
            //will hold tokens
            while((strLine = br.readLine()) != null ){
                splitLine = strLine.split("\\s+");
                //will split into tokens around spaces
                attribs = splitLine.length;
                //number of tokens per line is number of attribs + result
                myarr = new int[attribs-1];
                attrib = attribs-1;
                int result = 0;
                count = 0;
                //prepare to cast and copy tokens to my array
                while(attribs > count)
                {
                   if(count == 0)
                   {
                       if(splitLine[0].contentEquals("+1") == true)
                       {
                           //resultIn.add(true);
                           result = 1;
                       }
                       else
                       {
                           //resultIn.add(false);
                           result = 0;
                       }
                           //result = Character.getNumericValue(splitLine[0].charAt(1));
                       //if(result == 1)
                       //{
                         //  resultIn.add(true);
                       //}
                       //else
                       //{
                        //   resultIn.add(false);
                       //System.err.println("out");
                       //}
                   }
                   else
                   {
                            myarr[count-1] = Integer.parseInt(splitLine[count]); // = Integer.parseInt(splitLine[count]));
                   }
                       ++count;
                }
                if(result == 1)
                    liveList.add(myarr);
                else
                    dieList.add(myarr);
                //my array is added as the newest record
            }
            fstream.close();
            br.close();
            samples = liveList.size() + dieList.size();
        } catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }
    }
    private static void ReadTesting(String file){
        try {
            int count;
            FileInputStream fstream = new FileInputStream(file);
            //file has been loaded to fstream
            BufferedReader br = new BufferedReader(new InputStreamReader(new DataInputStream(fstream)));
            //buffered reader is ready
            String strLine = "";
            //initialized
            String[] splitLine;
            int[] myarr;
            //debugging vars
            //int test =0;
            
            //will hold tokens
            while((strLine = br.readLine()) != null ){
                splitLine = strLine.split("\\s+");
                //will split into tokens around spaces
                //number of tokens per line is number of attribs + result
                myarr = new int[attrib];
                int result = 0;
                count = 0;
                //prepare to cast and copy tokens to my array
                while(attrib >= count)
                {
                   if(count == 0)
                   {
                       if(splitLine[0].contentEquals("+1") == true)
                       {
                           //resultIn.add(true);
                           result = 1;
                       }
                       else
                       {
                           //resultIn.add(false);
                           result = 0;
                       }
                           //result = Character.getNumericValue(splitLine[0].charAt(1));
                       //if(result == 1)
                       //{
                         //  resultIn.add(true);
                       //}
                       //else
                       //{
                        //   resultIn.add(false);
                       //System.err.println("out");
                       //}
                   }
                   else
                   {
                            myarr[count-1] = Integer.parseInt(splitLine[count]); // = Integer.parseInt(splitLine[count]));
                   }
                       ++count;
                }
                if(result == 1)
                    testIn.add(true);
                else
                    testIn.add(false);
                TestData.add(myarr);
                //my array is added as the newest record
            }
            fstream.close();
            br.close();
        } catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }
    }
    private static void RunTesting(String file){
        try {
            int count;
            int occurredL = 0;
            int occurredD = 0;
            int trIter = 0;
            int guess = 0;
            double plive = 1;
            double pdie = 1;
            FileInputStream fstream = new FileInputStream(file);
            //file has been loaded to fstream
            BufferedReader br = new BufferedReader(new InputStreamReader(new DataInputStream(fstream)));
            //buffered reader is ready
            String strLine = "";
            //initialized
            String[] splitLine;
            int[] myarr;
            //debugging vars
            //int test =0;
            
            //will hold tokens
            while((strLine = br.readLine()) != null ){
                plive = 1.0;
                pdie = 1.0;
                splitLine = strLine.split("\\s+");
                //will split into tokens around spaces
                //number of tokens per line is number of attribs + result
                myarr = new int[attrib];
                int result = 0;
                count = 0;
                //prepare to cast and copy tokens to my array
                while(attrib >= count)
                {
                   if(count == 0)
                   {
                       if(splitLine[0].contentEquals("+1") == true)
                       {
                           //resultIn.add(true);
                           result = 1;
                       }
                       else
                       {
                           //resultIn.add(false);
                           result = 0;
                       }
                   }
                   else
                   {
                            myarr[count-1] = Integer.parseInt(splitLine[count]); // = Integer.parseInt(splitLine[count]));
                   }
                       ++count;
                }
                count = 0;
                while(count < attrib)
                {
                    trIter = 0;
                    occurredL = 0;
                    occurredD = 0;
                    while(trIter < samples)
                    {
                        if(liveList.size() > trIter)
                        {
                            if(myarr[count] == liveList.get(trIter)[count])
                            {
                                ++occurredL;
                            }
                        }
                        if(dieList.size() > trIter)
                        {
                            if(myarr[count] == dieList.get(trIter)[count])
                            {
                                ++occurredD;
                            }
                        }
                        if(trIter > dieList.size() && trIter >liveList.size())
                            trIter = samples;
                        else
                            ++trIter;
                    }
                    //check for need of laplacian
                    //if(occurredL == 0)
                    //{
                    //    plive = (1.0/(double)(samples+1))*plive;
                    //}
                    //else
                    //{
                       plive = ((double)(occurredL+1)/(double)(samples+1))*plive; 
                    //}
                    //if(occurredD == 0)
                    //{
                     //   pdie = (1.0/(double)(samples+1))*pdie;
                    //}
                    //else
                    //{
                        pdie = ((double)(occurredD+1)/(double)(samples+1))*pdie;
                    //}
                    //compute probs aggreggately, initally they start as 1                    
                    ++count;
                    trIter = 0;
                    occurredD = 0;
                    occurredL = 0;
                }
                plive = plive*((double)liveList.size()/(double)samples);
                pdie = pdie*((double)dieList.size()/(double)samples);
                if(plive > pdie)
                {
                    guess = 1;
                }
                else
                {
                    guess = 0;
                }
                if(guess == result)
                {
                    if(guess == 0)
                    {
                        ++trueneg;
                    }
                    else
                    {
                        ++truepos;
                    }
                }
                else
                {
                    if(guess == 0)
                    {
                        ++falseneg;
                        System.out.println("whoa");
                    }
                    else
                    {
                        ++falsepos;
                    }
                }
       
                //my array holds the current test case
                //first, take probability of each attrib living and dieing
                //multiply together aggregately, finally multiply master prob
                //higher value holds prediction
            }
            fstream.close();
            br.close();
        } catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }
    }
}
