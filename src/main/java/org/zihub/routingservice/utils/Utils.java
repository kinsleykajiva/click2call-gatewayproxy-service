package org.zihub.routingservice.utils;


import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import org.zihub.routingservice.pojos.JWTDataPojo;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;


import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

@Slf4j
public class Utils {

    public static String BaseUrl = "";
    public static String BaseFilesUrl = "";
    public static String WEBSITE_BaseUrl = "";

    public interface SERVICES_NAMES {
        String WEBSITE_BASEURL = "https://xxxxxxxxxx.com/";
        String BaseUrl = Utils.BaseUrl;
        String NOTIFICATIONS_SERVICE = BaseUrl + "notifications-service/";
    }
    static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
//  // 2021-03-24 16:48:05
    private static final SimpleDateFormat sdf3 = new SimpleDateFormat(DATE_FORMAT);






    public static Date stringDateToDate(String StrDate) {
        Date dateToReturn = null;
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

        try {
            dateToReturn = (Date)dateFormat.parse(StrDate);
        }
        catch (ParseException e) {
            e.printStackTrace();
        }

        return dateToReturn;
    }

    public static final MediaType okJSON	= MediaType.parse("application/json; charset=utf-8");
    public static  String getTimeStamp(){
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        return sdf3.format(timestamp);
    }

 public static  Date getTimeStampDateObject(){
     java.sql.Date date = new java.sql.Date(Calendar.getInstance().getTime().getTime());
     java.sql.Timestamp timestamp = new java.sql.Timestamp(date.getTime());
    return new java.sql.Date(timestamp.getTime());

    }


    public static JWTDataPojo tokenDecoder(HttpServletRequest request, String secreteKey){
        String jwt = request.getHeader("Authorization").replace("Bearer", "").trim();
        Algorithm algorithm = Algorithm.HMAC256(secreteKey);
        JWTVerifier verifier = JWT.require(algorithm).build();
        DecodedJWT decodedJWT = verifier.verify(jwt);


        int userId = (decodedJWT.getClaim("userId").asInt());
        int role = Integer.parseInt(decodedJWT.getClaims().get("role")+"");

        log.info("loading claims ");


        int companyId = (decodedJWT.getClaim("companyId").asInt());
        String fullName = (decodedJWT.getClaim("fullName").asString());
        return new JWTDataPojo(userId,role,companyId,fullName,jwt);
    }

    public static String notifyNewUserToJoinTeam(JWTDataPojo jwtDataPojo, String emailAddress, String teamName, int teamId){

        try {
            String json = new JSONObject()
                    .put("email", emailAddress)
                    .put("activateLink", SERVICES_NAMES.WEBSITE_BASEURL +"/auth?owner-id="+jwtDataPojo.getUserId()+"&company-id="+jwtDataPojo.getCompanyId()+"&team-id="+teamId)
                    .put("teamName", "<strong>"+teamName+"</strong>")
                    .put("addedByUser", jwtDataPojo.getFullName())
                    .put("name", "")
                    .toString();
            makeRequestServices(jwtDataPojo, SERVICES_NAMES.NOTIFICATIONS_SERVICE + "users/user-added-to-team", json);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    public static String notifyUserToJoinedTeam(JWTDataPojo jwtDataPojo, String emailAddress, String teamName, int teamId, String name){

        try {
            String json = new JSONObject()
                    .put("email", emailAddress)
                    .put("activateLink", SERVICES_NAMES.WEBSITE_BASEURL +"/view-settings?team-id="+teamId)
                    .put("teamName", "<strong>"+teamName+"</strong>")
                    .put("addedByUser", jwtDataPojo.getFullName())
                    .put("name", "<strong>"+name+"</strong>")
                    .toString();
            makeRequestServices(jwtDataPojo, SERVICES_NAMES.NOTIFICATIONS_SERVICE + "users/user-added-to-team", json);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }


    @NotNull
    public static String makeRequestServices(JWTDataPojo jwtDataPojo, final String url, String json) throws IOException {
        RequestBody body = RequestBody.create(json, okJSON); // new
        System.out.println("url "+url);
        var request =new Request.Builder()
              //  .url(SERVICES_NAMES.NOTIFICATIONS_SERVICE + url)
                .addHeader("Authorization" , "Bearer " + ( jwtDataPojo == null ? "" : jwtDataPojo.getJwt()) )
                .url( url)
                .post(body)
                .build();

        OkHttpClient client = new OkHttpClient();
        Response response = client.newCall(request).execute();
        String res = response.body().string();
        response.close();
        return res ;
    }
    @NotNull
    public static String makeRequestServices( final String url, String json) throws IOException {
        RequestBody body = RequestBody.create(json, okJSON); // new

        var request =new Request.Builder()
                .url( url)
                .post(body)
                .build();

        OkHttpClient client = new OkHttpClient();
        Response response = client.newCall(request).execute();
        String res = response.body().string();
        response.close();
        return res ;
    }



    public  static String randomString(int len){
         SecureRandom rnd = new SecureRandom();
         final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        StringBuilder sb = new StringBuilder(len);
        for(int i = 0; i < len; i++)
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        return sb.toString();
    }



    public static String  uploadFileToAWSS3(String path, String fileName, Optional<Map<String, String>> optionalMetaData, InputStream inputStream){

        AWSCredentials awsCredentials =
                new BasicAWSCredentials("xxxxxxxxxxx",
                        "xxxxxxxxxx");

        AmazonS3 s3client = AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .withRegion(Regions.AP_SOUTH_1)
                .build();


        ObjectMetadata objectMetadata = new ObjectMetadata();
        optionalMetaData.ifPresent(map -> {
            if (!map.isEmpty()) {
                map.forEach(objectMetadata::addUserMetadata);
            }
        });
        try {

            PutObjectResult result =  s3client.putObject(path, fileName, inputStream, objectMetadata);

            return   s3client.getUrl("clicktocallbucket/agents/profiles",path).toString();
           // return  s3client.getUrl("clicktocallbucket/profiles",path).toExternalForm();
        } catch (AmazonServiceException e) {
            throw new IllegalStateException("Failed to upload the file", e);
        }
    }

    public static String uploadFileToAWSS3Node(String folder, MultipartFile file) throws IOException {
        var fileTaget = multipartToFile(file,folder);
        MediaType MEDIA_TYPE_MARKDOWN
                = MediaType.parse("text/x-markdown; charset=utf-8");
       // post fil okhttp
        String fileName = file.getOriginalFilename();
        String fileType = file.getContentType();
        long fileSize = file.getSize();

        String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1);
        String fileNameWithoutExtension = fileName.substring(0, fileName.lastIndexOf("."));
        String fileNameWithExtension = fileNameWithoutExtension + "." + fileExtension;
        String fileNameWithExtensionAndSize = fileNameWithoutExtension + "." + fileExtension + "." + fileSize;
        String fileNameWithExtensionAndSizeAndType = fileNameWithoutExtension + "." + fileExtension + "." + fileSize + "." + fileType;
        String fileNameWithExtensionAndType = fileNameWithoutExtension + "." + fileExtension + "." + fileType;
      //  okhtttp upload file
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("folder", folder)
                .addFormDataPart("file", randomString(121)+ fileNameWithExtension.trim().replace(" ","_"), RequestBody.create(MEDIA_TYPE_MARKDOWN, fileTaget))
                .build();

        Request request = new Request.Builder()
                .url(BaseFilesUrl)
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            String responze  = response.body().string() ;
            System.out.println(response);

            return responze;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public  static File multipartToFile(MultipartFile multipart, String fileName) throws IllegalStateException, IOException {
        File convFile = new File(System.getProperty("java.io.tmpdir")+"/"+fileName);
        multipart.transferTo(convFile);
        return convFile;
    }
}
