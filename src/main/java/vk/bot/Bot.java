package vk.bot;

import com.vk.api.sdk.callback.longpoll.responses.GetLongPollEventsResponse;
import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.exceptions.LongPollServerKeyExpiredException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.groups.responses.GetLongPollServerResponse;
import com.vk.api.sdk.queries.messages.MessagesSendQuery;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import vk.russian.VkUser;
import vk.russian.WallActionsSpider;

public class Bot extends Thread {
    private final Integer VK_GROUP_ID = 108051944;
    private final String VK_GROUP_ACCESS_TOKEN = "e3ce880adfbddf28e1559c7d6b3aadc78a403ad4a53bf85b6069b084de4b7c3095dd66ba87ad7ab8c90ea";
    private volatile WallActionsSpider wallSpider;

    public Bot(WallActionsSpider wallSpider) {

        this.wallSpider = wallSpider;

    }

    public void run() {
        System.out.println(VK_GROUP_ID);
        TransportClient transportClient = HttpTransportClient.getInstance();
        VkApiClient vk = new VkApiClient(transportClient);
        GroupActor groupActor = new GroupActor(VK_GROUP_ID, VK_GROUP_ACCESS_TOKEN);
        try {
            GetLongPollServerResponse getLongPollServerResponse = vk.groups().getLongPollServer(groupActor).execute();
            Integer ts = getLongPollServerResponse.getTs();
            String key = getLongPollServerResponse.getKey();
            while (true) {
                GetLongPollEventsResponse longPoolResponse = vk.longPoll().getEvents(getLongPollServerResponse.getServer(), getLongPollServerResponse.getKey(), ts)
                        .waitTime(25)
                        .execute();
                ts = longPoolResponse.getTs();
                if (longPoolResponse.getUpdates().size() > 0 && longPoolResponse.getUpdates().toString().contains("message_new")) {
                    JSONParser updatesParser = new JSONParser();
                    try {
                        JSONObject updateObj = (JSONObject) updatesParser.parse(longPoolResponse.getUpdates().get(0).toString());
                        JSONObject messageObj = (JSONObject) updatesParser.parse(updateObj.get("object").toString());
                        Integer sendMassage;
                        System.out.println(messageObj.get("user_id") + " - написал:" + messageObj.get("body"));
                        String msg = "САМЫЕ АКТИВНЫЕ \uD83D\uDC47 \n";
                        switch (messageObj.get("body").toString().toLowerCase()) {
                            case "топ":
                                if (wallSpider.topActivityUsers.size() > 0) {
                                    int counter = 1;
                                    for (VkUser topActivityUser : wallSpider.topActivityUsers) {
                                        msg = msg + counter++ + ") [id" + topActivityUser.getId() + "|"
                                                + topActivityUser.getFirstName() + "] "
                                                + " - ❤" + topActivityUser.getLikesCount()
                                                + " - \uD83D\uDCAC" + topActivityUser.getCommentsCount() + "\n";
                                    }
                                    msg = msg + "\n ЛУЧШИЕ КОММЕНТАТОРЫ\uD83D\uDC47 \n";
                                    counter = 1;
                                    for (VkUser topCommentsLikesUser : wallSpider.topCommentsLikesUsers) {
                                        msg = msg + counter++ + ") [id" + topCommentsLikesUser.getId() + "|"
                                                + topCommentsLikesUser.getFirstName()+ "] "
                                                + " - \uD83D\uDCAC❤" + topCommentsLikesUser.getCommentsLikesCount() + "\n";
                                    }
                                } else {
                                    msg = "Попробуйте позже..";
                                }
                                System.out.println(msg);
                                sendMassage = vk.messages().send(groupActor)
                                        .userId(Integer.parseInt(messageObj.get("user_id").toString()))
                                        .message(msg)
                                        .execute();
                                break;
                            case "я":
                                sendMassage = vk.messages().send(groupActor)
                                        .userId(Integer.parseInt(messageObj.get("user_id").toString()))
                                        .message("\uD83D\uDC47 Здесь будет видно тебя в топе...")
                                        .execute();
                                break;
                            default:
                                sendMassage = vk.messages().send(groupActor)
                                        .userId(Integer.parseInt(messageObj.get("user_id").toString()))
                                        .message("\uD83D\uDE12 \uD83D\uDE12 \uD83D\uDE12 Извини, но я тебя не понимаю :(\n -------------------\n\n\n Напиши - я, чтобы узнать свою статистику, или - топ, чтобы увидеть Топ 10 участников.")
                                        .peerId(Integer.parseInt(messageObj.get("user_id").toString()))
                                        .execute();
                                break;
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    } catch (LongPollServerKeyExpiredException e) {
                        getLongPollServerResponse = vk.groups().getLongPollServer(groupActor).execute();
                    }
                    System.out.println(longPoolResponse.getUpdates().get(0));
                }
            }
        } catch (ApiException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        }
    }

}
