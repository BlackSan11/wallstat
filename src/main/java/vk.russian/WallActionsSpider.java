package vk.russian;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.vk.api.sdk.client.AbstractQueryBuilder;
import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiAccessException;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ApiParamException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.base.BoolInt;
import com.vk.api.sdk.objects.likes.responses.GetListExtendedResponse;
import com.vk.api.sdk.objects.likes.responses.IsLikedResponse;
import com.vk.api.sdk.objects.users.User;
import com.vk.api.sdk.objects.users.UserMin;
import com.vk.api.sdk.objects.wall.WallComment;
import com.vk.api.sdk.objects.wall.WallPostFull;
import com.vk.api.sdk.objects.wall.responses.GetCommentsExtendedResponse;
import com.vk.api.sdk.objects.wall.responses.GetExtendedResponse;
import com.vk.api.sdk.objects.wall.responses.GetRepostsResponse;
import com.vk.api.sdk.queries.execute.ExecuteCodeQuery;
import com.vk.api.sdk.queries.likes.LikesType;
import com.vk.api.sdk.queries.wall.WallGetCommentsSort;
import jfork.nproperty.Cfg;
import jfork.nproperty.ConfigParser;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;


public class WallActionsSpider extends Thread {

    @Cfg
    private static int PARS_COMUNITY_ID = 0;
    @Cfg
    private static String VK_APP_DATA_SAVER = "";
    @Cfg
    private static String VK_USER_ACCESS_TOKEN = "";
    @Cfg
    private static int VK_USER_ID = 0;

    private long chechDateTimePeriod;
    private UserActor vkActor;
    private VkApiClient vk;
    private PublicHeader publicHeader;
    public LinkedList<VkUser> usersPasedFromWall = new LinkedList<>();
    public LinkedList<VkUser> topActivityUsers = new LinkedList<>();
    public LinkedList<VkUser> topCommentsLikesUsers = new LinkedList<>();
    private LinkedList<LinkedList<VkUser>> resultTops = new LinkedList<>();
    private DayOfWeek[] weekDay = {
            DayOfWeek.SUNDAY,
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY,
            DayOfWeek.SATURDAY,
    };
    private VkUser[] leadersNow = new VkUser[4];
    Long parseStartDateTime = null;
    Long prevParseStartDateTimeNow = null;

    public WallActionsSpider() throws NoSuchMethodException, InstantiationException, IllegalAccessException, IOException, InvocationTargetException {
        ConfigParser.parse(WallActionsSpider.class, "main.conf");
        TransportClient transportClient = HttpTransportClient.getInstance();
        vk = new VkApiClient(transportClient);
        vkActor = new UserActor(VK_USER_ID, VK_USER_ACCESS_TOKEN);
    }

    public void uploadDataToServer() {
        Gson gson = new Gson();
        String jsonData = gson.toJson(resultTops);
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(VK_APP_DATA_SAVER);

        List<NameValuePair> params = new ArrayList<NameValuePair>(2);
        params.add(new BasicNameValuePair("pwd", "c73web"));
        params.add(new BasicNameValuePair("comId", String.valueOf(PARS_COMUNITY_ID)));
        params.add(new BasicNameValuePair("data", jsonData));
        try {
            httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            //Execute and get the response.
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            resultTops = new LinkedList<>();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private LinkedList<VkUser> getTopUsers(LinkedList<VkUser> parsedUsers, Comparator comp, boolean mode) {
        Collections.sort(parsedUsers, comp);
        LinkedList<VkUser> topResult = new LinkedList();
        int usersCounter = 0;
        for (VkUser vkUser : parsedUsers) {
            if (usersCounter < 10) {
                vkUser.setMoreInfoAboutUser(vk, vkActor);
                topResult.add(vkUser);
            } else {
                break;
            }
            usersCounter++;
        }
        resultTops.add((LinkedList<VkUser>) parsedUsers.clone());
        return topResult;
    }


    public void run() {
        //testExe();
        LocalDate currentDate;
        LocalDate prevMonday;
        LocalDateTime prevMonday1;
        while (true) {
            currentDate = LocalDate.now();
            prevMonday = currentDate.with(TemporalAdjusters.previous(weekDay[1]));
            prevMonday1 = LocalDateTime.of(prevMonday.getYear(), prevMonday.getMonthValue(), prevMonday.getDayOfMonth(), 0, 0, 0);
            prevParseStartDateTimeNow = prevMonday1.toEpochSecond(ZoneOffset.UTC);
            if (parseStartDateTime == null || parseStartDateTime != prevParseStartDateTimeNow) {
                parseStartDateTime = prevParseStartDateTimeNow;
            }
            usersPasedFromWall = new LinkedList<>();
            topActivityUsers = new LinkedList<>();
            topCommentsLikesUsers = new LinkedList<>();
            parseWall(); //парсим стену
            topActivityUsers = getTopUsers(this.usersPasedFromWall, new VkUserActivityComparator(), false);
            topCommentsLikesUsers = getTopUsers(this.usersPasedFromWall, new VkUserCommentsLikesComparator(), true);
            uploadDataToServer();
            leadersNow = new VkUser[]{
                    (topActivityUsers.size() > 0) ? topActivityUsers.get(0) : new VkUser(),
                    (topActivityUsers.size() > 1) ? topActivityUsers.get(1) : new VkUser(),
                    (topCommentsLikesUsers.size() > 0) ? topCommentsLikesUsers.get(0) : new VkUser(),
                    (topCommentsLikesUsers.size() > 1) ? topCommentsLikesUsers.get(1) : new VkUser()
            };
            try {
                publicHeader = new PublicHeader(vk, vkActor, leadersNow[0], leadersNow[1], leadersNow[2], leadersNow[3]);
                publicHeader.mainRender();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(60000 * 5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Получаем все посты
     */
    private void parseWall() {
        long unixTimeNow = System.currentTimeMillis() / 1000L;
        GetExtendedResponse posts;
        int count = 0;
        int offset = 0;
        int limit = 100;
        boolean timeIsUsed = false;
        int ito = 0;
        while (true) {
            try {
                Thread.sleep(350);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                posts = vk.wall().getExtended(this.vkActor)
                        .ownerId(-PARS_COMUNITY_ID)
                        .count(limit)
                        .offset(offset)
                        .fields()
                        .execute();
                if (posts.getCount() > 0) {
                    if (posts.getItems() != null) {
                        for (WallPostFull wallPostFull : posts.getItems()) {
                            if ((wallPostFull.getIsPinned() != null && wallPostFull.getIsPinned() == 1) || parseStartDateTime < wallPostFull.getDate()) {
                                fixLikes(wallPostFull.getId()); //фиксим лойсы
                                fixComments(wallPostFull.getId());
                            } else {
                                timeIsUsed = true;
                                break;
                            }
                            System.out.println(ito++);
                        }
                        if (timeIsUsed) break;
                    } else break;
                    //TODO: ниже тупая конструкция
                    if (count == 0) count = posts.getCount();
                    if (count <= offset) {
                        break;
                    } else offset += limit;
                } else {
                    //TODO:что если постов нет?
                    break;
                }
            } catch (ApiException e) {
                e.printStackTrace();
            } catch (ClientException e) {
                e.printStackTrace();
            }
        }
    }

    private void testExe(){
        LinkedList<AbstractQueryBuilder> toMethods = new LinkedList<>();
        String code = "return [API.wall.getComments({\"owner_id\":"+ -PARS_COMUNITY_ID +", \"post_id\":24248, \"need_likes\": \"1\", \"sort\": \"asc\", \"count\": \"100\", \"extended\": \"1\", \"count\": \"100\"}) + API.wall.getComments({\"owner_id\": "+ -PARS_COMUNITY_ID +", \"post_id\": 24248, \"need_likes\": \"1\", \"sort\": \"asc\", \"count\": \"100\", \"extended\": \"1\", \"count\": \"100\"})];";
        try {
            JsonElement execute = vk.execute().code(vkActor, code).execute();
            System.out.println(execute);
        } catch (ApiException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        }
        System.out.println("DDD");
    }

    private void fixLikes(int id) {
        int count = 0;
        int offset = 0;
        int limit = 1000;
        List temp = new LinkedList();
        GetListExtendedResponse likes;
        while (true) {
            try {
                Thread.sleep(350);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                likes = vk.likes().getListExtended(this.vkActor, LikesType.POST)
                        .ownerId(-PARS_COMUNITY_ID)
                        .itemId(id)
                        .count(limit)
                        .offset(offset)
                        .execute();
                if (likes.getCount() > 0) {
                    //System.out.println(likes.getCount());
                    if (likes.getItems() != null) {
                        for (UserMin userMin : likes.getItems()) {
                            List<VkUser> searchedUser = usersPasedFromWall
                                    .stream()
                                    .filter(vkUser -> vkUser.getId().equals(userMin.getId()))
                                    .limit(1)
                                    .collect(Collectors.toList());
                            if (searchedUser.size() > 0) {
                                searchedUser.get(0).upLikesCount();
                            } else {
                                VkUser vkUser = new VkUser();
                                vkUser.setId(userMin.getId());
                                vkUser.upLikesCount();
                                usersPasedFromWall.add(vkUser);
                            }
                        }
                    } else return;

                    //TODO: ниже тупая конструкция еще и к томуже добавляет цифры
                    if (count == 0) count = likes.getCount();
                    if (count <= offset || count <= limit) {
                        break;
                    } else offset += limit;
                } else {
                    //TODO:что если постов нет?
                    break;
                }

            } catch (ApiException e) {
                e.printStackTrace();
            } catch (ClientException e) {
                e.printStackTrace();
            }
        }
    }

    private void fixComments(int postID) {
        int count = 0;
        int offset = 0;
        int limit = 100;
        List temp = new LinkedList();
        GetCommentsExtendedResponse comments;
        while (true) {
            try {
                Thread.sleep(350);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                comments = vk.wall().getCommentsExtended(this.vkActor, postID)
                        .ownerId(-PARS_COMUNITY_ID)
                        .needLikes(true)
                        .sort(WallGetCommentsSort.ASC)
                        .count(limit)
                        .offset(offset)
                        .execute();
                if (comments.getCount() > 0) {
                    //System.out.println(comments.getCount());
                    if (comments.getItems() != null) {
                        for (WallComment comment : comments.getItems()) {
                            if(comment.getFromId() < 0) continue; //если это сообщество
                            List<VkUser> searchedUser = usersPasedFromWall
                                    .stream()
                                    .filter(vkUser -> vkUser.getId().equals(comment.getFromId()))
                                    .limit(1)
                                    .collect(Collectors.toList());
                            Thread.sleep(300);
                            IsLikedResponse likeComment = null;
                            //TODO: ниже лайкомент иисправитьм
                            try {
                                likeComment = vk.likes().isLiked(this.vkActor, LikesType.COMMENT, comment.getId())
                                        .ownerId(-PARS_COMUNITY_ID)
                                        .userId(comment.getFromId())
                                        .execute();
                            }catch (ApiAccessException e){
                                e.printStackTrace();
                            }catch (ApiParamException e){
                                System.out.println(comment.getFromId());
                                e.printStackTrace();
                            }

                            if (searchedUser.size() > 0) {
                                searchedUser.get(0).upCommentsCount();
                                if(comment.getLikes() != null){
                                    searchedUser.get(0).upCommentsLikesCount((likeComment != null && likeComment.isLiked()) ? comment.getLikes().getCount() - 1 : comment.getLikes().getCount());
                                }
                            } else {
                                VkUser vkUser = new VkUser();
                                vkUser.setId(comment.getFromId());
                                vkUser.upCommentsCount();
                                if(comment.getLikes() != null){
                                    vkUser.upCommentsLikesCount((likeComment != null && likeComment.isLiked()) ? comment.getLikes().getCount() - 1 : comment.getLikes().getCount());
                                }
                                usersPasedFromWall.add(vkUser);
                            }
                        }
                    } else return;

                    //TODO: ниже тупая конструкция еще и к томуже добавляет цифры
                    if (count == 0) count = comments.getCount();
                    if (count <= offset || count <= limit) {
                        break;
                    } else offset += limit;
                } else {
                    //TODO:что если постов нет?
                    break;
                }

            } catch (ApiException e) {
                e.printStackTrace();
            } catch (ClientException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void fixReposts(int postID) {
        int count = 0;
        int offset = 0;
        int limit = 100;
        List temp = new LinkedList();
        GetRepostsResponse reposts;
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                reposts = vk.wall().getReposts(this.vkActor)
                        .ownerId(-PARS_COMUNITY_ID)
                        .postId(postID)
                        .count(limit)
                        .offset(offset)
                        .execute();
                if (reposts.getProfiles().size() > 0) {
                    // System.out.println(reposts.getCount());
                    if (reposts.getProfiles() != null) {
                        for (User repost : reposts.getProfiles()) {
                            temp.add(repost.getFirstName() + " " + repost.getLastName());
                            List<VkUser> searchedUser = usersPasedFromWall
                                    .stream()
                                    .filter(vkUser -> vkUser.getId().equals(repost.getId()))
                                    .limit(1)
                                    .collect(Collectors.toList());
                            if (searchedUser.size() > 0) {
                                searchedUser.get(0).upRepostsCount();
                                //System.out.println(searchedUser.get(0).getFirstName() + " " + searchedUser.get(0).getLastName());
                            } else {
                                VkUser vkUser = new VkUser();
                                vkUser.setId(repost.getId());
                                vkUser.upRepostsCount();
                                usersPasedFromWall.add(vkUser);
                            }
                        }
                    } else return;

                    //TODO: ниже тупая конструкция еще и к томуже добавляет цифры
                    if (count == 0) count = reposts.getProfiles().size();
                    if (count <= offset || count <= limit) {
                        break;
                    } else offset += limit;
                } else {
                    //TODO:что если постов нет?
                    break;
                }

            } catch (ApiException e) {
                e.printStackTrace();
            } catch (ClientException e) {
                e.printStackTrace();
            }
        }
    }
}
