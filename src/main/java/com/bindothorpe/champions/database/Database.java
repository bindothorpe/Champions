package com.bindothorpe.champions.database;

import com.bindothorpe.champions.domain.build.Build;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.skill.SkillType;
import org.bukkit.Bukkit;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;

public class Database {

    private static Database instance = new Database();
    private Connection connection;

    private Database() {
    }

    public static Database getInstance() {
        if(instance == null)
            instance = new Database();
        return instance;
    }

    public Connection getConnection() throws SQLException {

        if (connection != null) {
            return connection;
        }

        //Try to connect to my MySQL database running locally
        String url = "jdbc:mysql://localhost/Champions";
        String user = "root";
        String password = "";

        Connection connection = DriverManager.getConnection(url, user, password);

        this.connection = connection;

        Bukkit.getLogger().log(Level.INFO, "Connected to database.");

        return connection;
    }

    public void initializeDatabase() throws SQLException {

        Statement statement = getConnection().createStatement();

        //Create the player_stats table
        String sql = "CREATE TABLE IF NOT EXISTS build (id VARCHAR(255) PRIMARY KEY, player_id VARCHAR(255), class_type VARCHAR(255), sword_skill VARCHAR(255), sword_level INT, axe_skill VARCHAR(255), axe_level INT, bow_skill VARCHAR(255), bow_level INT, passive_a_skill VARCHAR(255), passive_a_level INT, passive_b_skill VARCHAR(255), passive_b_level INT, passive_c_skill VARCHAR(255), passive_c_level INT, class_passive VARCHAR(255), skill_points INT)";
        statement.execute(sql);

        sql = "CREATE TABLE IF NOT EXISTS player (id VARCHAR(255) PRIMARY KEY, build_id VARCHAR(255), FOREIGN KEY (build_id) REFERENCES build(id) ON DELETE SET NULL)";
        statement.execute(sql);

        statement.close();

    }

    public String findSelectedBuildIdByUUID(String uuid) throws SQLException {

        PreparedStatement statement = getConnection().prepareStatement("SELECT * FROM player WHERE id = ?");
        statement.setString(1, uuid);

        ResultSet resultSet = statement.executeQuery();

        if (resultSet.next()) {

            String buildId = resultSet.getString("build_id");

            statement.close();

            return buildId;
        }

        statement.close();

        return null;
    }

    public void createPlayerSelectedBuild(String uuid, String buildId) throws SQLException {

        PreparedStatement statement = getConnection()
                .prepareStatement("INSERT INTO player(id, build_id) VALUES (?, ?)");
        statement.setString(1, uuid);
        statement.setString(2, buildId);

        statement.executeUpdate();

        statement.close();

    }

    public void updatePlayerSelectedBuild(String uuid, String buildId) throws SQLException {

        PreparedStatement statement = getConnection().prepareStatement("UPDATE player SET build_id = ? WHERE id = ?");
        statement.setString(1, buildId);
        statement.setString(2, uuid);

        statement.executeUpdate();

        statement.close();

    }

    public void deletePlayerSelectedBuild(String uuid) throws SQLException {

        PreparedStatement statement = getConnection().prepareStatement("DELETE FROM player WHERE id = ?");
        statement.setString(1, uuid);

        statement.executeUpdate();

        statement.close();

    }

    public List<Build> findBuildsByUUID(String uuid) throws SQLException {

        PreparedStatement statement = getConnection().prepareStatement("SELECT * FROM build WHERE player_id = ?");
        statement.setString(1, uuid);

        ResultSet resultSet = statement.executeQuery();

        List<Build> builds = new ArrayList<>();
        while (resultSet.next()) {

            Map<SkillType, SkillId> skills = new HashMap<>();
            skills.put(SkillType.SWORD, SkillId.valueOf(resultSet.getString("sword_skill")));
            skills.put(SkillType.AXE, SkillId.valueOf(resultSet.getString("axe_skill")));
            skills.put(SkillType.BOW, SkillId.valueOf(resultSet.getString("bow_skill")));
            skills.put(SkillType.PASSIVE_A, SkillId.valueOf(resultSet.getString("passive_a_skill")));
            skills.put(SkillType.PASSIVE_B, SkillId.valueOf(resultSet.getString("passive_b_skill")));
            skills.put(SkillType.PASSIVE_C, SkillId.valueOf(resultSet.getString("passive_c_skill")));
            skills.put(SkillType.CLASS_PASSIVE, SkillId.valueOf(resultSet.getString("class_passive")));

            Map<SkillType, Integer> skillLevels = new HashMap<>();
            skillLevels.put(SkillType.SWORD, resultSet.getInt("sword_level"));
            skillLevels.put(SkillType.AXE, resultSet.getInt("axe_level"));
            skillLevels.put(SkillType.BOW, resultSet.getInt("bow_level"));
            skillLevels.put(SkillType.PASSIVE_A, resultSet.getInt("passive_a_level"));
            skillLevels.put(SkillType.PASSIVE_B, resultSet.getInt("passive_b_level"));
            skillLevels.put(SkillType.PASSIVE_C, resultSet.getInt("passive_c_level"));
            skillLevels.put(SkillType.CLASS_PASSIVE, 1);

            Build build = new Build(resultSet.getString("id"), ClassType.valueOf(resultSet.getString("class_type")), skills, skillLevels, resultSet.getInt("skill_points"));
            builds.add(build);
        }

        statement.close();

        return builds;
    }

    public void createBuild(Build build, UUID ownerId) throws SQLException {
        PreparedStatement statement = getConnection()
                .prepareStatement("INSERT INTO build(" +
                        "id, player_id, class_type, " +
                        "sword_skill, sword_level," +
                        "axe_skill, axe_level," +
                        "bow_skill, bow_level," +
                        "passive_a_skill, passive_a_level," +
                        "passive_b_skill, passive_b_level," +
                        "passive_c_skill, passive_c_level," +
                        "class_passive, skill_points) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

        statement.setString(1, build.getId());
        statement.setString(2, ownerId.toString());
        statement.setString(3, build.getClassType().toString());
        statement.setString(4, build.getSkill(SkillType.SWORD).toString());
        statement.setInt(5, build.getSkillLevel(SkillType.SWORD));
        statement.setString(6, build.getSkill(SkillType.AXE).toString());
        statement.setInt(7, build.getSkillLevel(SkillType.AXE));
        statement.setString(8, build.getSkill(SkillType.BOW).toString());
        statement.setInt(9, build.getSkillLevel(SkillType.BOW));
        statement.setString(10, build.getSkill(SkillType.PASSIVE_A).toString());
        statement.setInt(11, build.getSkillLevel(SkillType.PASSIVE_A));
        statement.setString(12, build.getSkill(SkillType.PASSIVE_B).toString());
        statement.setInt(13, build.getSkillLevel(SkillType.PASSIVE_B));
        statement.setString(14, build.getSkill(SkillType.PASSIVE_C).toString());
        statement.setInt(15, build.getSkillLevel(SkillType.PASSIVE_C));
        statement.setString(16, build.getSkill(SkillType.CLASS_PASSIVE).toString());
        statement.setInt(17, build.getSkillPoints());

        statement.executeUpdate();

        statement.close();
    }


    public void updateBuild(Build build) throws SQLException {
        //Prepare the sql statement
        PreparedStatement statement = getConnection()
                .prepareStatement("UPDATE build SET " +
                        "sword_skill = ?, sword_level = ?, " +
                        "axe_skill = ?, axe_level = ?, " +
                        "bow_skill = ?, bow_level = ?, " +
                        "passive_a_skill = ?, passive_a_level = ?, " +
                        "passive_b_skill = ?, passive_b_level = ?, " +
                        "passive_c_skill = ?, passive_c_level = ?, " +
                        "skill_points = ? " +
                        "WHERE id = ?");

        //Set the values
        statement.setString(1, build.getSkill(SkillType.SWORD).toString());
        statement.setInt(2, build.getSkillLevel(SkillType.SWORD));
        statement.setString(3, build.getSkill(SkillType.AXE).toString());
        statement.setInt(4, build.getSkillLevel(SkillType.AXE));
        statement.setString(5, build.getSkill(SkillType.BOW).toString());
        statement.setInt(6, build.getSkillLevel(SkillType.BOW));
        statement.setString(7, build.getSkill(SkillType.PASSIVE_A).toString());
        statement.setInt(8, build.getSkillLevel(SkillType.PASSIVE_A));
        statement.setString(9, build.getSkill(SkillType.PASSIVE_B).toString());
        statement.setInt(10, build.getSkillLevel(SkillType.PASSIVE_B));
        statement.setString(11, build.getSkill(SkillType.PASSIVE_C).toString());
        statement.setInt(12, build.getSkillLevel(SkillType.PASSIVE_C));
        statement.setInt(13, build.getSkillPoints());


        statement.executeUpdate();

        statement.close();

    }

    public void deleteBuild(String id) throws SQLException {
        PreparedStatement statement = getConnection().prepareStatement("DELETE FROM build WHERE id = ?");
        statement.setString(1, id);
        statement.executeUpdate();
        statement.close();
    }


}
