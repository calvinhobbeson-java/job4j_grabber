package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.DateTimeParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse implements Parse {

    public static final Integer PAGES = 5;

    private static final Logger LOG = LoggerFactory.getLogger(HabrCareerParse.class.getName());

    private static final String SOURCE_LINK = "https://career.habr.com";

    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer?page=", SOURCE_LINK);

    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    private String retrieveDescription(String link) {
        Connection connection = Jsoup.connect(link);
        Document document;
        try {
            document = connection.get();
        } catch (IOException e) {
            LOG.error("Illegal argument exception", e);
            throw new IllegalArgumentException();
        }
        Elements desc = document.select(".style-ugc");
        return desc.text();
    }

    private Post parsePost(Element row) {
            Element titleElement = row.select(".vacancy-card__title").first();
            Element linkElement = titleElement.child(0);
            Element dateElement = row.select(".basic-date").first();
            String vacancyName = titleElement.text();
            String linkToVac = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
            String date = dateElement.attr("datetime");
            return new Post(vacancyName, linkToVac, retrieveDescription(SOURCE_LINK), dateTimeParser.parse(date));
    }

    public List<Post> list(String link) {
        List<Post> posts = new ArrayList<>();
        for (int pageNumber = 1; pageNumber <= PAGES; pageNumber++) {
            Connection connection = Jsoup.connect(PAGE_LINK + pageNumber);
            Document document;
            try {
                document = connection.get();
            } catch (IOException e) {
                LOG.error("Illegal argument exception", e);
                throw new IllegalArgumentException();
            }
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row ->
            posts.add(parsePost(row)));
        }
        return posts;
    }
}