import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class xmlReader {

    private static void readXML(String fname, FileWriter vld, FileWriter invld) {
        DocumentBuilderFactory dbf1 = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder docbuil = dbf1.newDocumentBuilder();

            Document doc = docbuil.parse(new File(fname));

            NodeList csrProd = doc.getElementsByTagName("CSR_Producer");
            System.out.println(csrProd.getLength());

            for (int i = 0; i < csrProd.getLength(); i++) {
                Node prodNode = csrProd.item(i);

                if (prodNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element producerElement = (Element) prodNode;

                    NodeList licenseList = producerElement.getElementsByTagName("License");

                    for (int j = 0; j < licenseList.getLength(); j++) {
                        Node licenseNode = licenseList.item(j);

                        if (licenseNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element licenseElement = (Element) licenseNode;

                            String licenseexpirationDate = licenseElement.getAttribute("License_Expiration_Date");

                            SimpleDateFormat a = new SimpleDateFormat("MM/dd/yyyy");

                            try {

                                Date edo = a.parse(licenseexpirationDate);

                                Date currenDate = new Date();

                                String nipr = producerElement.getAttribute("NIPR_Number");
                                String licenseNumber = licenseElement.getAttribute("License_Number");
                                String statecode = licenseElement.getAttribute("State_Code");
                                String effectivedate = licenseElement.getAttribute("Date_Status_Effective");

                                String info = nipr + ", " + statecode + ", " + licenseNumber + ", " + effectivedate;

                                if (edo.after(currenDate)) {

                                    vld.append(info);
                                    vld.append("\n");
                                } else {
                                    invld.append(info);
                                    invld.append("\n");
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                }
            }

        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
    }

    private static void mergefilefunction(String valid, String invalid) {
        try {
            PrintWriter pw = new PrintWriter("mergedFile.txt");

            BufferedReader br = new BufferedReader(new FileReader(valid));
            String line = br.readLine();
            while (line != null) {
                pw.println(line);
                line = br.readLine();
            }

            br = new BufferedReader(new FileReader(invalid));

            line = br.readLine();
            while (line != null) {
                pw.println(line);
                line = br.readLine();
            }
            pw.flush();
            br.close();
            pw.close();

        }

        catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void removeDuplicatesfunction(String outputfile, FileReader inputfile) {

        try {
            PrintWriter pw = new PrintWriter(outputfile);

            BufferedReader br1 = new BufferedReader(inputfile);

            String line1 = br1.readLine();

            while (line1 != null) {
                boolean flag = false;

                BufferedReader br2 = new BufferedReader(new FileReader(outputfile));

                String line2 = br2.readLine();

                while (line2 != null) {

                    if (line1.equals(line2)) {
                        flag = true;
                        break;
                    }

                    line2 = br2.readLine();

                }

                if (!flag) {
                    pw.println(line1);

                    pw.flush();
                }

                line1 = br1.readLine();

            }

            br1.close();
            pw.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {

        File valid = new File("validfile.txt");
        File invalid = new File("invalid.txt");
        try {
            FileWriter validWriter = new FileWriter(valid);
            FileWriter invalidWriter = new FileWriter(invalid);

            readXML("License1.xml", validWriter, invalidWriter);
            readXML("License2.xml", validWriter, invalidWriter);

            validWriter.close();
            invalidWriter.close();

            FileReader validReader = new FileReader(valid);
            FileReader invalidReader = new FileReader(invalid);

            String v = "validLicenses.txt";
            String i = "invalidLicenses.txt";
            removeDuplicatesfunction(v, validReader);
            removeDuplicatesfunction(i, invalidReader);

            mergefilefunction(v, i);

            validReader.close();
            invalidReader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}