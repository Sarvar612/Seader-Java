package org.Faker;

import com.github.javafaker.*;
import com.github.javafaker.service.RandomService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;

/**
 * Hello world!
 *
 */
public class AppForGeneratingDatas
{
    static final Map<FieldType,Supplier<Object>> functions=new HashMap<>();
    static final Scanner scan=new Scanner(System.in);
    static {
        Faker faker=new Faker();
        Name name = faker.name();
        Book book = faker.book();
        Country country = faker.country();
        Internet internet = faker.internet();
        PhoneNumber phoneNumber = faker.phoneNumber();
        RandomService random=faker.random();
        functions.put(FieldType.ID, random::nextLong);
        functions.put(FieldType.UUID, UUID::randomUUID);
        functions.put(FieldType.Blood_Group, name::bloodGroup);
        functions.put(FieldType.Age, ()->random.nextInt(0,100));
        functions.put(FieldType.Book_Title, book::title);
        functions.put(FieldType.Boot_Author,book::author);
        functions.put(FieldType.Post_Body, name::username);
        functions.put(FieldType.Phone, phoneNumber::cellPhone);
        functions.put(FieldType.FirstName,name::firstName);
        functions.put(FieldType.LastName, name::lastName);
        functions.put(FieldType.Words, faker::lorem);
        functions.put(FieldType.Letters, faker::lorem);
        functions.put(FieldType.Email,internet::emailAddress);
        functions.put(FieldType.Paragraphs, book::title);
        functions.put(FieldType.Capital,country::capital );
        functions.put(FieldType.CountryCode, country::countryCode2);
        functions.put(FieldType.Post_Title,book::title);

    }

    public static void main( String[] args ) throws IOException {

        Scanner sc = new Scanner(System.in);
        System.out.println("Enter file name:");
        String fileName = sc.next();
        System.out.println("Enter File Type(JSON, CSV,SQL) :");
        String type = sc.next();
        System.out.println("Enter Rows Count");
        int count = sc.nextInt();
        String temp = "";
        List<Pairs> pairs = new ArrayList<>();
        while(!temp.equals("s")){

            System.out.println("Enter the filed name:");
            String fieldName = sc.next();

            int t = 1;

            for (FieldType fieldType: FieldType.values()) {
                if((t+1)%2==0)
                    System.out.println();
                System.out.print(t++ +".  "+fieldType+"\t\t");
            }System.out.println("\nEnter the field type(1-17):");
            int flType = sc.nextInt();
            FieldType type_field = FieldType.values()[flType-1];
            pairs.add(new Pairs(fieldName,type_field));

            System.out.println("Add a file-> y(es)");
            System.out.println("Stop adding a file->s(top)");
            temp = sc.next();
        }

        Request request = new Request(fileName+"."+type,count,type,pairs);
        switch (type) {
            case "json" -> generateRandomDataAsJson(functions, request);
            case "csv" -> generateRandomDataAsCSV(functions, request);
            case "sql" -> generateRandomDataAsSQL(functions, request);
            default -> System.out.println("Sorry but we do not support this type of file!");
        }
        }


    private static void generateRandomDataAsCSV(Map<FieldType, Supplier<Object>> functions, Request request) throws IOException {
        List<Pairs> pairs = request.getPairs();
        StringBuilder div = new StringBuilder();
        String firstLine = "";

        for (int i = 0; i < pairs.size(); i++)
            firstLine += pairs.get(i).getFieldName()+((i!=pairs.size()-1)?",":"");

        for (int i = 0; i < request.getCount(); i++) {
            StringBuilder horizontal = new StringBuilder();
            for (Pairs pair : pairs) {
                FieldType fieldType = pair.getFieldType();
                Object value = functions.get(fieldType).get();
                String result = value.toString();
                if (result.contains(",")) {
                    result = "\"" + result + "\"";
                }
                horizontal.append(result).append(",");
            }
            div.append(horizontal.substring(0, horizontal.length() - 1)).append("\n");
        }
        String res = firstLine+"\n"+div;
        Files.writeString(Path.of(request.getFileName()),res);
        System.out.println(res);
    }

    private static void generateRandomDataAsJson(Map<FieldType, Supplier<Object>> functions, Request request) throws IOException {
        List<Pairs> pairs = request.getPairs();
        StringJoiner stringJoiner = new StringJoiner(", ","[\n","\n]");
        for (int i = 0; i < request.getCount(); i++) {
            StringJoiner stringJoiner1 = new StringJoiner(",\n","\n{\n","\n}");
            for(Pairs pair: pairs){
                FieldType fieldType = pair.getFieldType();
                stringJoiner1.add(fieldType.getJsonPairs(String.valueOf(fieldType), functions.get(fieldType).get()));
            }
            stringJoiner.add(stringJoiner1.toString());
        }
        Files.writeString(Path.of(request.getFileName()),stringJoiner.toString());
    }
    private static void generateRandomDataAsSQL(Map<FieldType, Supplier<Object>> functions, Request request) throws IOException{
        List<Pairs> pairs = request.getPairs();
        StringJoiner html = new StringJoiner("");
        String tableName ="INSERT INTO "+request.getFileName().replace(".","_")+" ";


        StringJoiner headHtml = new StringJoiner("");
        for (int i = 0; i < request.getCount(); i++) {
            StringJoiner keys = new StringJoiner(",","(",")");
            StringJoiner values = new StringJoiner(",","(",")");
            for (Pairs p : pairs) {
                FieldType fieldType = p.getFieldType();
                keys.add(p.getFieldName());
                values.add(fieldType.getSQLPairs(functions.get(fieldType).get()));
            }
            html.add(tableName);
            html.add(keys.toString());
             html.add(" VALUES "+values+";\n");
        }
        System.out.println(headHtml);
        Files.writeString(Path.of(request.getFileName()),headHtml.toString());
    }
}
