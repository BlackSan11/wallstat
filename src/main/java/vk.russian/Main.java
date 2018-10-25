package vk.russian;

import com.sun.corba.se.impl.orb.ORBConfiguratorImpl;
import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import jfork.nproperty.Cfg;
import jfork.nproperty.ConfigParser;
import vk.bot.Bot;


import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import java.util.TimeZone;

public class Main {
    @Cfg
    private static String VK_USER_ACCESS_TOKEN = "";
    @Cfg
    private static int VK_USER_ID = 0;

    public static void main(String[] args) throws ParseException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException, InvocationTargetException {
        ConfigParser.parse(Main.class, "main.conf");

        //{"expires_in":0,"access_token_131923632":"b66dc8ef4f182d28503873edbcef5dd120d676c3407a6b2c26ed9d7b83e71f5c637f21b7464349442b350"}
        String acc_tok = "b66dc8ef4f182d28503873edbcef5dd120d676c3407a6b2c26ed9d7b83e71f5c637f21b7464349442b350";


        String authStep1 = "https://oauth.vk.com/authorize?" +
                "client_id=6058644&" +
                "redirect_uri=http://pdpdpdp.site1&" +
                "display=mobile&" +
                "scope=market, email, stats, notifications, groups, docs, offline, ads, wall, notes, status, pages, stories, video, audio, photos, friends, notify&" +
                "v=5.78&" +
                "response_type=code";
        System.out.println(authStep1);
        //https://oauth.vk.com/authorize?client_id=6058644&display=page&redirect_uri=http://pdpdpdp.site1&scope=market, email, stats, notifications, groups, docs, offline, ads, wall, notes, status, pages, stories, video, audio, photos, friends, notify&response_type=code&v=5.84
        //https://oauth.vk.com/access_token?client_id=6058644&client_secret=fjz0BjY1Zih8haVt8eu4&redirect_uri=http://pdpdpdp.site1&code=4c94ed14c4018c4a7e


        WallActionsSpider wallActionsSpider = new WallActionsSpider();
        wallActionsSpider.start();
        //new Bot(wallActionsSpider).start();

    }
}