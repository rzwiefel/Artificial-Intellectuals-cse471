/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package classify;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Random;
import java.util.Scanner;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.Logistic;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.trees.DecisionStump;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

/**
 *
 * @author Sumner Van Schoyck
 */
public class Classifier {

    //for finding number of missing values in each row
    public static void missingValuesRows(Instances data) {
        int[] missingValues = new int[data.numInstances()];
        for(int i = 0; i < data.numInstances(); i++) {
            missingValues[i] = 0;
        }
        Instance example;
        String value = "";
        //get number of missing attributes per row
        int missValues = 0;
        for(int i = 0; i < data.numInstances(); i++) {
            example = data.instance(i);
            for(int j = 0; j < 15; j++) {
                if(example.attribute(j).isNominal()) {
                    value = example.stringValue(j);
                }
                else if(example.attribute(j).isNumeric()) {
                    value = Double.toString(example.value(j));
                }
                if(value.equals("?") || value.equals("NaN")) {
                    missingValues[i]++;
                    missValues++;
                }
            }
        }
        System.out.println("Number of Missing Values: " + missValues);
        //get how many times i attributes are missing
        int[] frequency = new int[15];
        for(int i = 0; i < data.numInstances(); i++) {
            frequency[missingValues[i]]++;
        }
        int numRows = 0;
        for(int i = 0; i < data.numInstances(); i++) {
            if(missingValues[i] > 0) {
                numRows++;
            }
        }
        System.out.println("Number of rows with missing values: " + numRows);
        System.out.println("Number of missing attributes per row:");
        for(int i = 0; i < 15; i++) {
            System.out.println(i + ": " + frequency[i]);
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //read in data
        try {
            DataSource input = new DataSource("no_missing_values.csv");
            Instances data = input.getDataSet();
            //Instances data = readFile("newfixed.txt");
            missingValuesRows(data);
            
            setAttributeValues(data);
            data.setClassIndex(data.numAttributes() - 1);
            
            
            //boosting
            AdaBoostM1 boosting = new AdaBoostM1();
            boosting.setNumIterations(25);
            boosting.setClassifier(new DecisionStump());
            
            //build the classifier
            boosting.buildClassifier(data);
            
            //evaluate using 10-fold cross validation
            Evaluation e1 = new Evaluation(data);
            e1.crossValidateModel(boosting, data, 10, new Random(1));
            
            DecimalFormat nf = new DecimalFormat("0.000");
            
            System.out.println("Results of Boosting with Decision Stumps:");
            System.out.println(boosting.toString());
            System.out.println("Results of Cross Validation:");
            System.out.println("Number of correctly classified instances: " + e1.correct() + " (" + nf.format(e1.pctCorrect()) + "%)");
            System.out.println("Number of incorrectly classified instances: " + e1.incorrect() + " (" + nf.format(e1.pctIncorrect()) + "%)");
            
            System.out.println("TP Rate: " + nf.format(e1.weightedTruePositiveRate()*100) + "%");
            System.out.println("FP Rate: " + nf.format(e1.weightedFalsePositiveRate()*100) + "%");
            System.out.println("Precision: " + nf.format(e1.weightedPrecision()*100) + "%");
            System.out.println("Recall: " + nf.format(e1.weightedRecall()*100) + "%");
            
            System.out.println();
            System.out.println("Confusion Matrix:");
            for(int i = 0; i < e1.confusionMatrix().length; i++) {
                for(int j = 0; j < e1.confusionMatrix()[0].length; j++) {
                    System.out.print(e1.confusionMatrix()[i][j] + "   ");
                }
                System.out.println();
            }
            System.out.println();
            System.out.println();
            System.out.println();
            
            
            //logistic regression
            Logistic l = new Logistic();
            l.buildClassifier(data);
            
            e1 = new Evaluation(data);
            
            e1.crossValidateModel(l, data, 10, new Random(1));
            System.out.println("Results of Logistic Regression:");
            System.out.println(l.toString());
            System.out.println("Results of Cross Validation:");
            System.out.println("Number of correctly classified instances: " + e1.correct() + " (" + nf.format(e1.pctCorrect()) + "%)");
            System.out.println("Number of incorrectly classified instances: " + e1.incorrect() + " (" + nf.format(e1.pctIncorrect()) + "%)");
            
            System.out.println("TP Rate: " + nf.format(e1.weightedTruePositiveRate()*100) + "%");
            System.out.println("FP Rate: " + nf.format(e1.weightedFalsePositiveRate()*100) + "%");
            System.out.println("Precision: " + nf.format(e1.weightedPrecision()*100) + "%");
            System.out.println("Recall: " + nf.format(e1.weightedRecall()*100) + "%");
            
            System.out.println();
            System.out.println("Confusion Matrix:");
            for(int i = 0; i < e1.confusionMatrix().length; i++) {
                for(int j = 0; j < e1.confusionMatrix()[0].length; j++) {
                    System.out.print(e1.confusionMatrix()[i][j] + "   ");
                }
                System.out.println();
            }
            
        } catch (Exception ex) {
            //data couldn't be read, so end program
            System.out.println("Exception thrown, program ending.");
        }
    }
    
    public static void setAttributeValues(Instances data) {
        Instance example;
        String[][] savedData = new String[data.numInstances()][10];
        for(int i = 0; i < data.numInstances(); i++) {
            example = data.instance(i);
            savedData[i][0] = example.stringValue(0);
            savedData[i][1] = example.stringValue(3);
            savedData[i][2] = example.stringValue(4);
            savedData[i][3] = example.stringValue(5);
            savedData[i][4] = example.stringValue(6);
            savedData[i][5] = example.stringValue(8);
            savedData[i][6] = example.stringValue(9);
            savedData[i][7] = example.stringValue(11);
            savedData[i][8] = example.stringValue(12);
            savedData[i][9] = example.stringValue(15);
        }

        //add in values for discrete attributes
        //A1
        FastVector attVals = new FastVector();
        attVals.addElement("b");
        attVals.addElement("a");
        data.deleteAttributeAt(0);
        data.insertAttributeAt(new Attribute("A1", attVals), 0);

        //A4
        attVals = new FastVector();
        attVals.addElement("u");
        attVals.addElement("y");
        attVals.addElement("l");
        attVals.addElement("t");
        data.deleteAttributeAt(3);
        data.insertAttributeAt(new Attribute("A4", attVals), 3);

        //A5
        attVals = new FastVector();
        attVals.addElement("g");
        attVals.addElement("p");
        attVals.addElement("gg");
        data.deleteAttributeAt(4);
        data.insertAttributeAt(new Attribute("A5", attVals), 4);

        //A6
        attVals = new FastVector();
        attVals.addElement("c");
        attVals.addElement("d");
        attVals.addElement("cc");
        attVals.addElement("i");
        attVals.addElement("j");
        attVals.addElement("k");
        attVals.addElement("m");
        attVals.addElement("r");
        attVals.addElement("q");
        attVals.addElement("w");
        attVals.addElement("x");
        attVals.addElement("e");
        attVals.addElement("aa");
        attVals.addElement("ff");
        data.deleteAttributeAt(5);
        data.insertAttributeAt(new Attribute("A6", attVals), 5);

        //A7
        attVals = new FastVector();
        attVals.addElement("v");
        attVals.addElement("h");
        attVals.addElement("bb");
        attVals.addElement("j");
        attVals.addElement("n");
        attVals.addElement("z");
        attVals.addElement("dd");
        attVals.addElement("ff");
        attVals.addElement("o");
        data.deleteAttributeAt(6);
        data.insertAttributeAt(new Attribute("A7", attVals), 6);

        //A9
        attVals = new FastVector();
        attVals.addElement("t");
        attVals.addElement("f");
        data.deleteAttributeAt(8);
        data.insertAttributeAt(new Attribute("A9", attVals), 8);

        //A10
        attVals = new FastVector();
        attVals.addElement("t");
        attVals.addElement("f");
        data.deleteAttributeAt(9);
        data.insertAttributeAt(new Attribute("A10", attVals), 9);

        //A12
        attVals = new FastVector();
        attVals.addElement("t");
        attVals.addElement("f");
        data.deleteAttributeAt(11);
        data.insertAttributeAt(new Attribute("A12", attVals), 11);

        //A13
        attVals = new FastVector();
        attVals.addElement("g");
        attVals.addElement("p");
        attVals.addElement("s");
        data.deleteAttributeAt(12);
        data.insertAttributeAt(new Attribute("A13", attVals), 12);

        //Class
        attVals = new FastVector();
        attVals.addElement("+");
        attVals.addElement("-");
        data.deleteAttributeAt(15);
        data.insertAttributeAt(new Attribute("C", attVals), 15);

        for(int i = 0; i < data.numInstances(); i++) {

            if(!"?".equals(savedData[i][0])) {
                data.instance(i).setValue(0, savedData[i][0]);
            }

            if(!"?".equals(savedData[i][1])) {
                data.instance(i).setValue(3, savedData[i][1]);
            }

            if(!"?".equals(savedData[i][2])) {
                data.instance(i).setValue(4, savedData[i][2]);
            }

            if(!"?".equals(savedData[i][3])) {
                data.instance(i).setValue(5, savedData[i][3]);
            }

            if(!"?".equals(savedData[i][4])) {
                data.instance(i).setValue(6, savedData[i][4]);
            }

            if(!"?".equals(savedData[i][5])) {
                data.instance(i).setValue(8, savedData[i][5]);
            }

            if(!"?".equals(savedData[i][6])) {
                data.instance(i).setValue(9, savedData[i][6]);
            }

            if(!"?".equals(savedData[i][7])) {
                data.instance(i).setValue(11, savedData[i][7]);
            }

            if(!"?".equals(savedData[i][8])) {
                data.instance(i).setValue(12, savedData[i][8]);
            }

            if(!"?".equals(savedData[i][9])) {
                data.instance(i).setValue(15, savedData[i][9]);
            }
        }
    }
}
