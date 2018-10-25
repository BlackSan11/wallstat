package vk.russian;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.users.CropPhoto;
import com.vk.api.sdk.objects.users.User;
import com.vk.api.sdk.objects.users.UserXtrCounters;
import com.vk.api.sdk.objects.wall.responses.GetExtendedResponse;
import com.vk.api.sdk.objects.wall.responses.GetResponse;
import com.vk.api.sdk.queries.users.UserField;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;

public class VkUser {
    public Integer getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    private Integer id;
    private String firstName = "Пусто";
    private String lastName;
    private transient CropPhoto cropPhotoObj;
    private transient BufferedImage cropPhotoImage;
    private transient BufferedImage anonImg;
    private Integer likesCount = 0;
    private Integer repostsCount = 0;
    private Integer commentsCount = 0;
    private Integer postsCount = 0;
    private Integer commentsLikesCount = 0;



    public void setActive(Boolean active) {
        this.active = active;
    }

    private transient Boolean active;

    public void setId(Integer id) {
        this.id = id;
    }

    public CropPhoto getCropPhotoObj() {
        return cropPhotoObj;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setCropPhoto(CropPhoto cropPhotoObj) {
        this.cropPhotoObj = cropPhotoObj;
    }

    public Integer getLikesCount() {
        return likesCount;
    }

    public Integer getRepostsCount() {
        return repostsCount;
    }

    public Integer getCommentsCount() {
        return commentsCount;
    }

    public Integer getPostsCount() {
        return postsCount;
    }


    public void upLikesCount() {
        this.likesCount++;
    }

    public void upRepostsCount() {
        this.repostsCount++;
    }

    public void upCommentsCount() {
        this.commentsCount++;
    }

    public void upCommentsLikesCount(int count) {
        this.commentsLikesCount = this.commentsLikesCount + count;
    }

    public void upPostsCount() {
        this.postsCount++;
    }

    public void setShortInfoAboutUser(VkApiClient vk, UserActor vkActor) {
        try {
            Thread.sleep(350);
            if(this.id != null){
                List<UserXtrCounters> user = vk.users().get(vkActor)
                        .userIds(String.valueOf(this.id))
                        .execute();
                this.firstName = user.get(0).getFirstName();
                this.lastName = user.get(0).getLastName();
            }
        } catch (ApiException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void setMoreInfoAboutUser(VkApiClient vk, UserActor vkActor) {
        try {
           Thread.sleep(300);
            if(this.id != null){
                List<UserXtrCounters> user = vk.users().get(vkActor)
                        .userIds(String.valueOf(this.id))
                        .fields(UserField.CROP_PHOTO)
                        .execute();
                this.firstName = user.get(0).getFirstName();
                this.lastName = user.get(0).getLastName();
                this.cropPhotoObj = (user.get(0).getCropPhoto() != null) ? user.get(0).getCropPhoto() : null;
            }
            if (cropPhotoObj != null) {
                this.cropPhotoImage = loadAvaImage(cropPhotoObj.getPhoto().getPhoto604());
            } else {
                try {
                    this.cropPhotoImage = ImageIO.read(new File(PublicHeader.PATH_TO_ANON_IMG));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (ApiException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private BufferedImage loadAvaImage(String urlToImg) {
        URL url = null;
        try {
            url = new URL(urlToImg);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        File file = null;
        BufferedImage img = null;
        try {
            img = ImageIO.read(url);
            int x, x1, y, y1;
            x = (int) ((this.cropPhotoObj.getCrop().getX() / 100) * img.getWidth());
            x1 = (int) ((this.cropPhotoObj.getCrop().getX2() / 100) * img.getWidth());
            y = (int) ((this.cropPhotoObj.getCrop().getY() / 100) * img.getHeight());
            y1 = (int) ((this.cropPhotoObj.getCrop().getY2() / 100) * img.getHeight());
            img = img.getSubimage(x, y, x1 - x, y1 - y);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return img;
    }

    public Integer getCommentsLikesCount() {
        return commentsLikesCount;
    }

    public BufferedImage getCropPhotoImage() {
        return cropPhotoImage;
    }
}

class VkUserActivityComparator implements Comparator<VkUser> {
    @Override
    public int compare(VkUser o1, VkUser o2) {
        return ((o2.getLikesCount() + o2.getCommentsCount()) - (o1.getLikesCount() + o1.getCommentsCount()));
    }
}

class VkUserCommentsLikesComparator implements Comparator<VkUser> {

    @Override
    public int compare(VkUser o1, VkUser o2) {
        return o2.getCommentsLikesCount() - o1.getCommentsLikesCount();
    }
}