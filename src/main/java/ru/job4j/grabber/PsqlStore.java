package ru.job4j.grabber;

import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PsqlStore implements Store, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(HabrCareerParse.class.getName());

    private Connection cnn;

    public Post fill(ResultSet resultSet) throws SQLException {
        return new Post(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getString("text"),
                resultSet.getString("link"),
                resultSet.getTimestamp("created").toLocalDateTime());
    }

    public PsqlStore(Properties cfg) {
        try {
            Class.forName(cfg.getProperty("jdbc.driver"));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        try {
            cnn = DriverManager.getConnection(
                    cfg.getProperty("url"),
                    cfg.getProperty("username"),
                    cfg.getProperty("password")
            );
        } catch (SQLException e) {
            LOG.error("SQL Exception", e);
            e.printStackTrace();
        }
    }

    @Override
    public void save(Post post) {
        try (PreparedStatement statement =
                     cnn.prepareStatement("insert into posts(name, text, link, created) values(?, ?, ?, ?)",
                             Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, post.getTitle());
            statement.setString(2, post.getDescription());
            statement.setString(3, post.getLink());
            statement.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
            statement.execute();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    post.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            LOG.error("SQL Exception", e);
                e.printStackTrace();
            }
        }

    @Override
    public List<Post> getAll() {
        List<Post> posts = new ArrayList<>();
        try (PreparedStatement statement = cnn.prepareStatement("select * from posts")) {
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    posts.add((fill(resultSet)));
                }
            }
        } catch (SQLException e) {
            LOG.error("SQL Exception", e);
            e.printStackTrace();
        }
        return posts;
    }

    @Override
    public Post findById(int id) {
        Post post = null;
        try (PreparedStatement statement = cnn.prepareStatement(
                "select * from posts where id = ?")) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    post = fill(resultSet);
                }
            }
        }  catch (SQLException e) {
            LOG.error("SQL Exception", e);
            e.printStackTrace();
        }
        return post;
    }

    @Override
    public void close() throws Exception {
        if (cnn != null) {
            cnn.close();
        }
    }

    public static void main(String[] args) {
        Properties cfg = new Properties();
        try (InputStream in = PsqlStore.class.getClassLoader().getResourceAsStream("grabber.properties")) {
            cfg.load(in);
            Class.forName(cfg.getProperty("driver-class-name"));
        } catch (Exception e) {
            LOG.error("SQL Exception", e);
            e.printStackTrace();
        }
        try (PsqlStore psqlStore = new PsqlStore(cfg)) {
            psqlStore.save(new Post("Programmist", "linkToVac",
                    "Programmist programmiruet", LocalDateTime.now()));
            psqlStore.save(new Post("otherPosition", "linkToOtherPosition",
                    "On delaet drugie veschi, ne programmiruet", LocalDateTime.now()));
            psqlStore.getAll();
            psqlStore.findById(1);
        } catch (Exception e) {
            LOG.error("SQL Exception", e);
            e.printStackTrace();
        }
    }
}