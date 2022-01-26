package edu.mcw.rgd.web;

import edu.mcw.rgd.dao.impl.RGDNewsConfDAO;
import edu.mcw.rgd.datamodel.RGDNewsConf;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @author mtutaj
 * @since Jan 26, 2022
 */
@RestController
@Api(tags="News")
@RequestMapping(value = "/news")

public class NewsWebService {

    RGDNewsConfDAO dao = new RGDNewsConfDAO();

    @RequestMapping(value="/last", method=RequestMethod.POST)
    @ApiOperation(value="Get a number of recent RGD news. Maximum ten news is returned unless 'limit' parameter is provided.", tags="News")
    public HashMap<String, HashMap<String,Object>> getLastNews(
            @ApiParam(value = "Maximum number of news items to be returned (optional)") @RequestParam(required = false) Integer limit

    ) throws Exception{

        int newsLimit = 10;
        if( limit!=null ) {
            newsLimit = limit;
            if( newsLimit<=0 ) {
                newsLimit = 1;
            }
        }

        HashMap<String, Object> result = new HashMap<>();

        HashMap<String, HashMap<String, Object> > resultSet = new HashMap<>();
        resultSet.put("resultset", result);

        result.put("api_version", "1.0");
        result.put("data_provider", "RGD");
        result.put("data_version", "RGD-2022-JAN");
        result.put("query_url", "https://rest.rgd.mcw.edu/rgdws/news/last");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        String timestamp = sdf.format(new Date());
        result.put("query_time", timestamp);

        List resultList = new ArrayList();
        result.put("result", resultList);

        HashMap newsMap = new HashMap();
        resultList.add(newsMap);

        ArrayList newsList = new ArrayList();
        newsMap.put("news", newsList);

        List<RGDNewsConf> allNews = dao.getAllNews();
        for( int i=0; i<allNews.size() && i<newsLimit; i++ ) {

            RGDNewsConf newsInRgd = allNews.get(i);
            String link = newsInRgd.getRedirectLink();
            if( !link.startsWith("http") ) {
                link = "https://rgd.mcw.edu" + link;
            }

            HashMap news = new HashMap();
            news.put("link", link);
            news.put("excerpt", newsInRgd.getDisplayText());
            news.put("title", newsInRgd.getDisplayText());
            newsList.add(news);
        }

        return resultSet;
    }
}
