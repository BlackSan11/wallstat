package vk.russian;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.photos.responses.GetOwnerCoverPhotoUploadServerResponse;
import com.vk.api.sdk.objects.photos.responses.PhotosSaveOwnerCoverPhotoResponse;
import jfork.nproperty.Cfg;
import jfork.nproperty.ConfigParser;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PublicHeader {

    private VkApiClient vk;
    private UserActor userActor;

    //лидеры активности
    private VkUser firActLead;
    private VkUser secActLead;

    //лидеры комментариев
    private VkUser firComLikeLead;
    private VkUser secComLikeLead;

    File sourceHeaderImgFile;
    File destinationHeaderImgFile;

    BufferedImage sourceHeaderBufImg;
    BufferedImage likeIconBufImage;
    BufferedImage commsIconBufImage;
    BufferedImage repsIconBufImage;
    BufferedImage commsLikesIconBufImage;

    //размеры шапки-обложки вк
    @Cfg
    private static int VK_HEADER_WEIGHT = 1590;
    @Cfg
    private static int VK_HEADER_HEIGHT = 400;
    @Cfg
    private static int HEADER_CHANGE_GROUP_ID = 0;


    //размеры иконок лидеров
    @Cfg
    private static int FIRST_LEADER_SIZE = 200;
    @Cfg
    private static int SECOND_LEADER_SIZE = (FIRST_LEADER_SIZE / 2) + 40;

    //размеры отступов между иконками
    @Cfg
    private static int SPACE_IN_NOMINATION = 75;
    @Cfg
    private static int SPACE_FROM_NAMINATION = 200;

    //координаты отображения аватарок лидеров на шапке
    @Cfg
    private static int FIRST_LEADER_PHOTO_Y = 97;
    @Cfg
    private static int SECOND_LEADER_PHOTO_Y = 167;
    //      по активности
    private int firActLeadPhotoX;
    private int secActLeadPhotoX;
    //      по комментариям
    private int firComLikeLeadPhotoX;
    private int secComLikeLeadPhotoX;


    //координаты отображения надписей лидеров на шапке
    @Cfg
    private static int FIRST_LEADER_NAME_Y = 88;
    @Cfg
    private static int SECOND_LEADER_NAME_Y = 156;
    @Cfg
    private static int FIRST_LEADER_COUNTERS_Y = 310;
    @Cfg
    private static int SECOND_LEADER_COUNTERS_Y = 315;

    //обводка для фото
    @Cfg
    private static int PHOTO_STROKE_WEIGHT = 4;
    private final Color PHOTO_STROKE_COLOR = new Color(0f, 0f, 0f, 1f);

    //текст
    @Cfg
    private static String NAMES_COUNTERS_FONT = "Bahnschrift Light SemiCondensed";
    private final Color NAMES_COUNTERS_TEXT_COLOR = Color.white;
    @Cfg
    private static int FIRST_LEADER_TEXT_SIZE = 27;
    @Cfg
    private static int SECOND_LEADER_TEXT_SIZE = 24;

    //иконки счетчиков
    //размер иконки
    @Cfg
    private static int FIRST_LEADER_ICONS_SIZE = 30;
    @Cfg
    private static int SECOND_LEADER_ICONS_SIZE = 24;
    //отступ от иконки к цифре
    @Cfg
    private static int FIRST_LEADER_SPACE_FROM_ICON = 2;
    @Cfg
    private static int SECOND_LEADER_SPACE_FROM_ICON = 2;
    //отступ от цифры к иконке
    @Cfg
    private static int FIRST_LEADER_SPACE_FROM_DIG = 6;
    @Cfg
    private static int SECOND_LEADER_SPACE_FROM_DIG = 4;
    @Cfg
    private static String ACTIONS_TITLE;
    @Cfg
    private static String COMMENTS_LIKES_COUNT_TITLE;
    @Cfg
    private static String UPDATED_TIME_TEXT;
    @Cfg
    private static String TITLES_TEXT_FONT = "Bahnschrift Light SemiCondensed";
    @Cfg
    private final Color TITLES_TEXT_COLOR = Color.white;
    @Cfg
    private static int TITLES_TEXT_SIZE = 33;
    @Cfg
    private static int TITLES_TEXT_Y = 380;

    //пути
    @Cfg
    private static String IMGS_PATH;
    @Cfg
    private static String ICONS_PATH = IMGS_PATH + "icons/";
    @Cfg
    private static String SOURCE_HEADER_PATH = IMGS_PATH + "in.jpg";
    @Cfg
    private static String RESOULT_HEADER_PATH = IMGS_PATH + "out.jpg";

    @Cfg
    private static String LIKES_ICON_PATH = ICONS_PATH + "like.png";
    @Cfg
    private static String COMMENTS_ICON_PATH = ICONS_PATH + "comm.png";
    @Cfg
    private static String REPOSTS_ICON_PATH = ICONS_PATH + "rep.png";
    @Cfg
    private static String COMMENT_LIKES_ICON_PATH = ICONS_PATH + "commslikes.png";
    @Cfg
    public static String PATH_TO_ANON_IMG = "imgs/anon.jpg";

    public PublicHeader(VkApiClient vk, UserActor userActor, VkUser firActLead, VkUser secActLead, VkUser firComLikeLead, VkUser secComLikeLead) throws IOException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        ConfigParser.parse(PublicHeader.class,new InputStreamReader(
                new FileInputStream("main.conf"), "UTF-8"),"main.conf");
        this.vk = vk;
        this.userActor = userActor;
        this.firActLead = firActLead;
        this.secActLead = secActLead;
        this.firComLikeLead = firComLikeLead;
        this.secComLikeLead = secComLikeLead;

        sourceHeaderImgFile = new File(SOURCE_HEADER_PATH);
        destinationHeaderImgFile = new File(RESOULT_HEADER_PATH);

        sourceHeaderBufImg = ImageIO.read(sourceHeaderImgFile);
        likeIconBufImage = ImageIO.read(new File(LIKES_ICON_PATH));
        commsIconBufImage = ImageIO.read(new File(COMMENTS_ICON_PATH));
        repsIconBufImage = ImageIO.read(new File(REPOSTS_ICON_PATH));
        commsLikesIconBufImage = ImageIO.read(new File(COMMENT_LIKES_ICON_PATH));

        int allElementsWeight = (FIRST_LEADER_SIZE * 2) + (SECOND_LEADER_SIZE * 2) + (SPACE_IN_NOMINATION * 2) + SPACE_FROM_NAMINATION;

        secActLeadPhotoX = (int) ((VK_HEADER_WEIGHT / 2) - (allElementsWeight / 2));
        firActLeadPhotoX = secActLeadPhotoX + SECOND_LEADER_SIZE + SPACE_IN_NOMINATION;
        firComLikeLeadPhotoX = firActLeadPhotoX + FIRST_LEADER_SIZE + SPACE_FROM_NAMINATION;
        secComLikeLeadPhotoX = firComLikeLeadPhotoX + FIRST_LEADER_SIZE + SPACE_IN_NOMINATION;

    }

    public void mainRender() throws IOException {

        Graphics2D hdrImgG = sourceHeaderBufImg.createGraphics();

        //отрисовываем аватарки
        hdrImgG.drawImage(addCorners(firActLead.getCropPhotoImage(), FIRST_LEADER_SIZE), firActLeadPhotoX, FIRST_LEADER_PHOTO_Y, FIRST_LEADER_SIZE, FIRST_LEADER_SIZE, null);
        hdrImgG.drawImage(addCorners(secActLead.getCropPhotoImage(), SECOND_LEADER_SIZE), secActLeadPhotoX, SECOND_LEADER_PHOTO_Y, SECOND_LEADER_SIZE, SECOND_LEADER_SIZE, null);
        hdrImgG.drawImage(addCorners(firComLikeLead.getCropPhotoImage(), FIRST_LEADER_SIZE), firComLikeLeadPhotoX, FIRST_LEADER_PHOTO_Y, FIRST_LEADER_SIZE, FIRST_LEADER_SIZE, null);
        hdrImgG.drawImage(addCorners(secComLikeLead.getCropPhotoImage(), SECOND_LEADER_SIZE), secComLikeLeadPhotoX, SECOND_LEADER_PHOTO_Y, SECOND_LEADER_SIZE, SECOND_LEADER_SIZE, null);

        //отрисовка текста для аватарок#############
        hdrImgG.setColor(NAMES_COUNTERS_TEXT_COLOR);
        hdrImgG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


        printLeaderCountersInfo(hdrImgG, firActLead, firActLeadPhotoX, FIRST_LEADER_COUNTERS_Y, 0, 0);
        printLeaderCountersInfo(hdrImgG, secActLead, secActLeadPhotoX, SECOND_LEADER_COUNTERS_Y, 1, 0);

        printLeaderCountersInfo(hdrImgG, firComLikeLead, firComLikeLeadPhotoX, FIRST_LEADER_COUNTERS_Y, 0, 1);
        printLeaderCountersInfo(hdrImgG, secComLikeLead, secComLikeLeadPhotoX, SECOND_LEADER_COUNTERS_Y, 1, 1);

        hdrImgG.setFont(new Font(TITLES_TEXT_FONT, Font.PLAIN, TITLES_TEXT_SIZE));
        hdrImgG.setColor(TITLES_TEXT_COLOR);
        hdrImgG.drawString(ACTIONS_TITLE, ((firActLeadPhotoX + secActLeadPhotoX + FIRST_LEADER_SIZE) / 2) - ((hdrImgG.getFontMetrics().stringWidth(ACTIONS_TITLE)) / 2), TITLES_TEXT_Y);
        hdrImgG.drawString(COMMENTS_LIKES_COUNT_TITLE, ((firComLikeLeadPhotoX + secComLikeLeadPhotoX + SECOND_LEADER_SIZE) / 2) - ((hdrImgG.getFontMetrics().stringWidth(COMMENTS_LIKES_COUNT_TITLE)) / 2), TITLES_TEXT_Y);

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        System.out.println("Обновлено = " + sdf.format(new Date()));
        hdrImgG.setFont(new Font(TITLES_TEXT_FONT, Font.BOLD, TITLES_TEXT_SIZE / 2));
        hdrImgG.drawString(UPDATED_TIME_TEXT + sdf.format(new Date()), (VK_HEADER_WEIGHT / 2) - ((hdrImgG.getFontMetrics().stringWidth(UPDATED_TIME_TEXT + sdf.format(new Date()))) / 2), 20);


        hdrImgG.dispose();

        ImageIO.write(sourceHeaderBufImg, "png", destinationHeaderImgFile);
        uploadHeaderToVk();
    }

    //ищем стартовую позицию X для счетчиков
    //
    private int[] getAllXCountersPositions(FontMetrics fm, VkUser vkUser, int photoX, int size, int mode) {

        int[] iconSize;
        if (mode == 0) {
            iconSize = new int[]{
                    FIRST_LEADER_ICONS_SIZE,
                    SECOND_LEADER_ICONS_SIZE
            };
        } else {
            iconSize = new int[]{
                    FIRST_LEADER_ICONS_SIZE + 10,
                    SECOND_LEADER_ICONS_SIZE + 10
            };
        }

        int[] imageWeight = {
                FIRST_LEADER_SIZE,
                SECOND_LEADER_SIZE
        };

        int iconsTotWeight = iconSize[size] * 2;

        int[] spaceFromIcon = {
                FIRST_LEADER_SPACE_FROM_ICON,
                SECOND_LEADER_SPACE_FROM_ICON
        };

        int[] spaceFromDig = {
                FIRST_LEADER_SPACE_FROM_DIG,
                SECOND_LEADER_SPACE_FROM_DIG
        };
        int[] positionX;
        if (mode == 1) {
            int startPos = photoX + (imageWeight[size] / 2) -
                    ((fm.stringWidth(String.valueOf(vkUser.getCommentsLikesCount()))
                            + (spaceFromIcon[size])) / 2);

            positionX = new int[]{
                    startPos,
                    startPos + iconSize[size] + spaceFromIcon[size],
                    photoX + ((imageWeight[size] - fm.stringWidth(vkUser.getFirstName())) / 2)
            };
        } else {
            int startPos = photoX + (imageWeight[size] / 2) -
                    ((fm.stringWidth(String.valueOf(vkUser.getLikesCount())) +
                            fm.stringWidth(String.valueOf(vkUser.getCommentsCount())) +
                            +iconsTotWeight + (spaceFromIcon[size] * 2) + (spaceFromDig[size])) / 2);

            positionX = new int[]{
                    startPos,
                    startPos + iconSize[size] + spaceFromIcon[size],
                    startPos + iconSize[size] + spaceFromIcon[size] + fm.stringWidth(String.valueOf(vkUser.getLikesCount())) + spaceFromDig[size],
                    startPos + (iconSize[size] * 2) + (spaceFromIcon[size] * 2) + fm.stringWidth(String.valueOf(vkUser.getLikesCount())) + spaceFromDig[size],
                    photoX + ((imageWeight[size] - fm.stringWidth(vkUser.getFirstName())) / 2)
            };
        }
        return positionX;
    }

    private void printLeaderCountersInfo(Graphics2D hdrImgG, VkUser vkUser, int photoX, int countersY, int size, int mode) {
        int[] iconSize = {
                FIRST_LEADER_ICONS_SIZE,
                SECOND_LEADER_ICONS_SIZE
        };

        int[] leadNameY = {
                FIRST_LEADER_NAME_Y,
                SECOND_LEADER_NAME_Y
        };

        int[] textSize = {
                FIRST_LEADER_TEXT_SIZE,
                SECOND_LEADER_TEXT_SIZE
        };

        hdrImgG.setFont(new Font(NAMES_COUNTERS_FONT, Font.CENTER_BASELINE, textSize[size]));

        int[] countElX = getAllXCountersPositions(hdrImgG.getFontMetrics(), vkUser, photoX, size, mode);

        int strHeight = hdrImgG.getFontMetrics().getHeight();


        if (mode == 1) {
            hdrImgG.drawString(vkUser.getFirstName(), countElX[2], leadNameY[size]);
            hdrImgG.drawImage(this.commsLikesIconBufImage, countElX[0], countersY, iconSize[size] + 10, iconSize[size] + 10, null);
            hdrImgG.drawString(String.valueOf(vkUser.getCommentsLikesCount()), countElX[1], countersY + ((iconSize[size] / 2) + (strHeight / 5)));
        } else {
            hdrImgG.drawString(vkUser.getFirstName(), countElX[4], leadNameY[size]);
            hdrImgG.drawImage(this.likeIconBufImage, countElX[0], countersY, iconSize[size], iconSize[size], null);
            hdrImgG.drawString(String.valueOf(vkUser.getLikesCount()), countElX[1], countersY + ((iconSize[size] / 2) + (strHeight / 5)));
            hdrImgG.drawImage(this.commsIconBufImage, countElX[2], countersY, iconSize[size], iconSize[size], null);
            hdrImgG.drawString(String.valueOf(vkUser.getCommentsCount()), countElX[3], countersY + ((iconSize[size] / 2) + (strHeight / 5)));

        }
    }

    private BufferedImage addCorners(BufferedImage tempImg, int cornerRadius) {
        BufferedImage tempImgRounded = new BufferedImage(cornerRadius, cornerRadius, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = tempImgRounded.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        graphics2D.fill(new RoundRectangle2D.Float(0, 0, cornerRadius, cornerRadius, cornerRadius, cornerRadius));
        graphics2D.setComposite(AlphaComposite.SrcAtop);

        graphics2D.drawImage(tempImg, 0, 0, cornerRadius, cornerRadius, null);
        graphics2D.setColor(PHOTO_STROKE_COLOR);
        graphics2D.setStroke(new BasicStroke(PHOTO_STROKE_WEIGHT));
        graphics2D.draw(new RoundRectangle2D.Float(0, 0, cornerRadius, cornerRadius, cornerRadius, cornerRadius));
        graphics2D.dispose();
        return tempImgRounded;
    }

    public void uploadHeaderToVk() {
        try {
            GetOwnerCoverPhotoUploadServerResponse response = vk.photos().getOwnerCoverPhotoUploadServer(userActor, HEADER_CHANGE_GROUP_ID)
                    .cropX(0)
                    .cropX2(VK_HEADER_WEIGHT)
                    .cropY(0)
                    .cropY2(VK_HEADER_HEIGHT)
                    .execute();

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(response.getUploadUrl());

            try {
                FileBody bin = new FileBody(new File(RESOULT_HEADER_PATH));
                MultipartEntity reqEntity = new MultipartEntity();
                reqEntity.addPart("photo", bin);

                httppost.setEntity(reqEntity);
                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                String responseBody = httpclient.execute(httppost, responseHandler);

                JSONObject jObject = new JSONObject(responseBody); // json

                String hash = jObject.getString("hash"); // get the name from data.
                String photo = jObject.getString("photo"); // get the name from data.
                PhotosSaveOwnerCoverPhotoResponse response1 = vk.photos().saveOwnerCoverPhoto(userActor, photo, hash)
                        .execute();

            } catch (ClientProtocolException e) {

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                httpclient.getConnectionManager().shutdown();
            }

        } catch (ApiException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        }
    }

}
