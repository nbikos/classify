/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
import java.util.Random;

/**
 *
 * @author nick
 */
public class NBAdaboost {
    static int k = 5;
    static ArrayList<int[]> trainList = new ArrayList<int[]>();
    static ArrayList<int[]> testList = new ArrayList<int[]>();
    static ArrayList<Boolean> trainRes = new ArrayList<Boolean>();
    static ArrayList<int[]> poss = new ArrayList<int[]>();
    static ArrayList<int[]> liveList;;
    static ArrayList<int[]> dieList;
    static int attrib = 0;//
    static int elements = 0;//
    static int hitIter = 0;
    static int truepos = 0;
    static int trueneg = 0;
    static int falsepos = 0;
    static int falseneg = 0;
    static double[] trainWeights;//
    static int[][][] setHolder;//
    static double[] errorHolder;//
    static double[] classWeight;
    static Boolean[][] setRes;
    static double[] CDF;
    static int[][] mySample;
    static int[] hashes;//connects random samples to main training data
    static int[][] sizes;
    static int[] presize;
    static double oldsum = 0.0;
    static boolean[][] testres;
    
   
    
    
    public static void main(String[] args){
        if (args.length != 2){
               System.err.println("$ java Classify Train.txt Test.txt");
               System.exit(1);
        }
           int count = 0;
           int pager = 0;
           double errorrunsum = 0.0;
           double chooseL = 0.0;
           double chooseD = 0.0;
           ReadTraining(args[0]);
           ReadTest(args[1]);
           MakeStruc();
           settoLD(trainList);
           AllPossible();
           //need holder for current CDF
           while(hitIter < k)
           {
           sample();
           settoLD(mySample);
           Laplace();
           falsepos = 0;
           falseneg = 0;
           truepos = 0;
           trueneg = 0;
           RunTest();
           count = 0;
           errorrunsum = 0.0;
           
           while(count < elements)
           {
               if(setRes[hitIter][count]==Boolean.FALSE)
               {
                   errorrunsum = errorrunsum + trainWeights[hashes[count]];
               }
               ++count;
           }
           if(errorrunsum <= 0.6)
           {
               errorHolder[hitIter] = errorrunsum;
               count = 0;
               oldsum = 0;
               while(count < elements)
               {
                   oldsum = oldsum + (double)trainWeights[count];
                   ++count;
               }
               count = 0;
               setHolder[hitIter] = mySample;
               sizes[hitIter] = presize;
               classWeight[hitIter] = Math.log((1-errorHolder[hitIter])/errorHolder[hitIter]);
               while(count < elements)
               {
                   if((setRes[hitIter][count]))
                   {
                       trainWeights[hashes[count]] = trainWeights[hashes[count]]*(errorHolder[hitIter])/(1-errorHolder[hitIter]);
                   }
                   ++count;
               }
               count = 0;
               errorrunsum = 0.0; //using to hold new weight sum
               while(count < elements)
               {
                   errorrunsum = errorrunsum + trainWeights[count];
                   ++count;
               }
               count = 0;
               while(count < elements)
               {
                   trainWeights[count] = (double)trainWeights[count]*oldsum/errorrunsum;
                   ++count;
               }
               updateCDF();
               ++hitIter;
               errorrunsum = 0.0;
           }
           errorrunsum = 0.0;
    }
           //travel through hit list, for every 0, look up location in hashes
           //use that location to find weight, sum all weights together
           //if error is > .5, commit no change, start over
           //else set error on proper page
           //else travel through hit list, look for every 1, change weight
           //normalize all weights, 
           //update CDF
           //commit mysample to "book"
           //commit presizes to right sizes page
           //calculate weight of classifer and commit
           //return to start
           
           count = 0;
           while(count < k)
           {
           settoLD(setHolder[count]);
           RunTester(sizes[count], count);
           ++count;
           }
           count = 0;
           truepos = 0;
           trueneg = 0;
           falsepos = 0;
           falseneg = 0;
           while(count < testList.size())
           {
               pager = 0;
               while(pager < k)
               {
                        if(testres[pager][count] == Boolean.TRUE)
                            chooseL = chooseL + Math.abs(classWeight[pager]);
                        else
                            chooseD = chooseD + Math.abs(classWeight[pager]);
                    ++pager;
               }
               if(chooseL >= chooseD)
               {
                   if(testList.get(count)[0] == 1)
                       ++truepos;
                   else
                       ++falsepos;
               }
               else//wrong
               {
                   if(testList.get(count)[0] == 0)
                       ++trueneg;
                   else
                       ++falseneg;
               }
               chooseL = 0.0;
               chooseD = 0.0;
               
               ++count;
           }
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
           //randomly pick "elements" using CDF method

           //laplace new training set
           //each classifier must track its attribs sizes
           
           //test training set
           //load each item, test against original set
           //need table to hold hits and misses
           //error = sum of misclassified weights
           //if error rate is acceptable, commit changes to holder
           //use hit table to adjust weights
           //normalize all weights
           //how is the weight per set calculated?
           //log*(1-error)\(error)
           //repeat k  times
        
    }
    private static void RunTester(int[] sizer, int cur)
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
       while(testIt < testList.size())//test data
       {
           result = (testList.get(testIt)[0] == 1);
           count = 0;
           while(count < attrib)
           {    
                occurredD = 0;
                occurredL = 0;
                trIt = 0;
                while(trIt < liveList.size())
                {
                      if(liveList.get(trIt)[count] == testList.get(testIt)[count+1])
                        {
                             ++occurredL;
                        }
                        ++trIt;
                }
                trIt = 0;
                while(trIt < dieList.size())
                {
                    if(dieList.get(trIt)[count] == testList.get(testIt)[count+1])
                        {
                             ++occurredD;
                        }
                    ++trIt;
                }
                if(occurredL == 0)
                {
                    //System.out.println("there were no survivors :(");
                    plive = (double)1/(double)sizer[count]*plive;
                }
                else
                {
                    plive = (double)(occurredL+1)/(double)sizer[count]*plive;
                }
                if(occurredD == 0)
                {
                    //System.out.println("there were no dead! whoooop!");
                    pdie = (double)1/(double)sizer[count]*pdie;
                }
                else
                {
                    pdie = (double)(occurredD+1)/(double)sizer[count]*pdie;
                }
                ++count;
            }
           if(liveList.size() == 0)
           {
               plive = (double)plive/(double)(elements+1);
           }
           else
           {
               plive = (double)liveList.size()*plive/(double)elements;
           }
           if(dieList.size() == 0)
           {
               pdie = (double)pdie/(double)(elements+1);
           }
           else
           {
               pdie = (double)dieList.size()*pdie/(double)elements;
           }
           if(plive>=pdie)
               guess = true;
           else
               guess = false;
           if(guess == result)
           {
               testres[cur][testIt] = Boolean.TRUE;
           }
           else
           {
               testres[cur][testIt] = Boolean.FALSE;
           }
           plive = 1.0;
           pdie = 1.0;
           ++testIt;
       }
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
       while(testIt < mySample.length)//test data
       {
           result = (mySample[testIt][0]==1);
           count = 0;
           while(count < attrib)
           {    
                occurredD = 0;
                occurredL = 0;
                trIt = 0;
                while(trIt < liveList.size())
                {
                      if(liveList.get(trIt)[count] == mySample[testIt][count+1])
                        {
                             ++occurredL;
                        }
                        ++trIt;
                }
                trIt = 0;
                while(trIt < dieList.size())
                {
                    if(dieList.get(trIt)[count] == mySample[testIt][count+1])
                        {
                             ++occurredD;
                        }
                    ++trIt;
                }
                if(occurredL == 0)
                {
                    //System.out.println("there were no survivors :(");
                    plive = (double)1/(double)presize[count]*plive;
                }
                else
                {
                    plive = (double)(occurredL+1)/(double)presize[count]*plive;
                }
                if(occurredD == 0)
                {
                    //System.out.println("there were no dead! whoooop!");
                    pdie = (double)1/(double)presize[count]*pdie;
                }
                else
                {
                    pdie = (double)(occurredD+1)/(double)presize[count]*pdie;
                }
                ++count;
            }
           if(liveList.size() == 0)
           {
               plive = (double)1/(double)(elements+1)*plive;
           }
           else
           {
               plive = (double)liveList.size()/(double)elements*plive;
           }
           if(dieList.size() == 0)
           {
               pdie = (double)1/(double)(elements+1)*pdie;
           }
           else
           {
               pdie = (double)dieList.size()/(double)elements*pdie;
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
               setRes[hitIter][testIt] = Boolean.TRUE;
           }
           else
           {
               if(guess == false)
                   ++falseneg;//miss
               else
                   ++falsepos;
               setRes[hitIter][testIt] = Boolean.FALSE;
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
       presize = new int[attrib];
       Arrays.fill(presize, elements);
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
                   //System.out.println("whooooop");
                   ++presize[count];
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
                   //System.out.println("whooooop dead");
                   ++presize[count];
               }
               occurredD = 0;
               ++possIt;
           }
           
           ++count;
       }
       Arrays.fill(presize, elements+attrib);
   }
    private static void settoLD(int[][] in)
    {
        liveList = new ArrayList<int[]>();
        dieList = new ArrayList<int[]>();
        int count = 0;
        while(count < elements){
            if(in[count][0] == 1)
                liveList.add(in[count]);
            else
                dieList.add(in[count]);
            ++count;
        }
    }
    private static void settoLD(ArrayList<int[]> in)
    {
        liveList = new ArrayList<int[]>();
        dieList = new ArrayList<int[]>();
        int count = 0;
        while(count < elements){
            if(in.get(count)[0] == 1)
                liveList.add(in.get(count));
            else
                dieList.add(in.get(count));
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
                    if(possin.contains(liveList.get(dataIt)[count+1]))
                    {
                        //skip
                    }
                    else
                    {
                        possin.add(liveList.get(dataIt)[count+1]);
                    }
                }
               else
               {
                   possin.add(liveList.get(dataIt)[count+1]);
               }
               ++dataIt;
            }
           dataIt = 0;
           while(dataIt < dieList.size())
           {
               if(!(possin.isEmpty()))
               {
                    if(possin.contains(dieList.get(dataIt)[count+1]))
                    {
                        //skip
                    }
                    else
                    {
                        possin.add(dieList.get(dataIt)[count+1]);
                    }
                }
               else
               {
                   possin.add(dieList.get(dataIt)[count+1]);
               }
               ++dataIt;
           }
           dataIt = 0;
           while(dataIt < testList.size())
           {
               if(!(possin.isEmpty()))
               {
                    if(possin.contains(testList.get(dataIt)[count+1]))
                    {
                        //skip
                    }
                    else
                    {
                        possin.add(testList.get(dataIt)[count+1]);
                    }
                }
               else
               {
                   possin.add(testList.get(dataIt)[count+1]);
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
    private static void sample(){
        int count = 0;
        int search = 0;
        Random maker = new Random();
        double rand = 0.0;
        while(count < elements){
            rand = maker.nextDouble();
            //get a random number between 1 and 0
            search = Arrays.binarySearch(CDF, rand);
            if(search < 0)
            {
                search = (search+1)*(-1);
            }
            //binary search the CDF list
            hashes[count] = search;
            mySample[count] = trainList.get(search);
            //pull that listing from training data to mysample
            ++count;
        }
        
    }
    private static void MakeStruc(){
        int count = 0;
        trainWeights = new double[elements];
        while(count < elements){
            trainWeights[count] = (double)1/(double)elements;
            ++count;            
        }
        setHolder = new int[k][elements][attrib+1];
        errorHolder = new double[k];
        classWeight = new double[k];
        setRes = new Boolean[k][elements];
        CDF = new double[elements];
        updateCDF();
        mySample = new int[elements][attrib+1];
        hashes = new int[elements];
        sizes = new int[k][attrib];
        testres = new boolean[k][testList.size()];
        count = 0;
        int iter = 0;
        while(count < k){
            while(iter < attrib){
                sizes[count][iter] = elements;
                ++iter;
            }
            ++count;
        }
    }
    private static void updateCDF(){
        int count = 0;
        while(count < elements){
            if(count == 0)
            {
                CDF[0] = trainWeights[0];
            }
            else
            {
                CDF[count] = trainWeights[count] + CDF[count-1];
            }
            ++count;
        }
    }
    private static void ReadTraining(String file){
        try {
            int attribs = 0;
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
                attribs = splitLine.length;
                //number of tokens per line is number of attribs + result
                myarr = new int[attribs];
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
                           myarr[0] = 1;
                           //trainRes.add(Boolean.TRUE);
                       }
                       else
                       {
                           //resultIn.add(false);
                           result = 0;
                           myarr[0] = 0;
                           //trainRes.add(Boolean.FALSE);
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
                            myarr[count] = Integer.parseInt(splitLine[count]); // = Integer.parseInt(splitLine[count]));
                   }
                       ++count;
                }
                if(result == 1)
                    trainList.add(myarr);
                else
                    trainList.add(myarr);
                //my array is added as the newest record
            }
            elements = trainList.size();
            fstream.close();
            br.close();
        } catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }
    }
    private static void ReadTest(String file){
        try {
            int attribs = 0;
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
                attribs = splitLine.length;
                //number of tokens per line is number of attribs + result
                myarr = new int[attribs];
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
                           myarr[0] = 1;
                           //trainRes.add(Boolean.TRUE);
                       }
                       else
                       {
                           //resultIn.add(false);
                           result = 0;
                           myarr[0] = 0;
                           //trainRes.add(Boolean.FALSE);
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
                            myarr[count] = Integer.parseInt(splitLine[count]); // = Integer.parseInt(splitLine[count]));
                   }
                       ++count;
                }
                if(result == 1)
                    testList.add(myarr);
                else
                    testList.add(myarr);
                //my array is added as the newest record
            }
            fstream.close();
            br.close();
        } catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }
    }
    
}

