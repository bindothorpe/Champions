package com.bindothorpe.champions.database.repository;

import com.bindothorpe.champions.domain.build.Build;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.skill.SkillType;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

public class BuildRepository {

    private final Connection connection;

    public BuildRepository(Connection connection) {
        this.connection = connection;
    }

    public String findSelectedBuildIdByUUID(String uuid) throws SQLException {

        PreparedStatement statement = connection.prepareStatement("SELECT * FROM player WHERE id = ?");
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

        PreparedStatement statement = connection.prepareStatement("INSERT INTO player(id, build_id) VALUES (?, ?)");
        statement.setString(1, uuid);
        statement.setString(2, buildId);

        statement.executeUpdate();

        statement.close();

    }

    public void updatePlayerSelectedBuild(String uuid, String buildId) throws SQLException {

        PreparedStatement statement = connection.prepareStatement("UPDATE player SET build_id = ? WHERE id = ?");
        statement.setString(1, buildId);
        statement.setString(2, uuid);

        statement.executeUpdate();

        statement.close();
    }

    public String findPlayerSelectedBuildByUUID(String uuid) throws SQLException {

        PreparedStatement statement = connection.prepareStatement("SELECT * FROM player WHERE id = ?");
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

    public List<Build> findBuildsByUUID(String uuid) throws SQLException {

        PreparedStatement statement = connection.prepareStatement("SELECT * FROM build WHERE player_id = ?");
        statement.setString(1, uuid);

        ResultSet resultSet = statement.executeQuery();

        List<Build> builds = new ArrayList<>();
        while (resultSet.next()) {

            try {
                Map<SkillType, SkillId> skills = new HashMap<>();
                skills.put(SkillType.SWORD, resultSet.getString("sword_skill") != null ? SkillId.valueOf(resultSet.getString("sword_skill")) : null);
                skills.put(SkillType.AXE, resultSet.getString("axe_skill") != null ? SkillId.valueOf(resultSet.getString("axe_skill")) : null);
                skills.put(SkillType.BOW, resultSet.getString("bow_skill") != null ? SkillId.valueOf(resultSet.getString("bow_skill")) : null);
                skills.put(SkillType.PASSIVE_A, resultSet.getString("passive_a_skill") != null ? SkillId.valueOf(resultSet.getString("passive_a_skill")) : null);
                skills.put(SkillType.PASSIVE_B, resultSet.getString("passive_b_skill") != null ? SkillId.valueOf(resultSet.getString("passive_b_skill")) : null);
                skills.put(SkillType.PASSIVE_C, resultSet.getString("passive_c_skill") != null ? SkillId.valueOf(resultSet.getString("passive_c_skill")) : null);
                skills.put(SkillType.CLASS_PASSIVE, resultSet.getString("class_passive") != null ? SkillId.valueOf(resultSet.getString("class_passive")) : null);

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
            } catch (IllegalArgumentException e) {
                Bukkit.getLogger().log(Level.WARNING, "Failed to load build " + resultSet.getString("id") + " for player " + uuid + " due to invalid skill id");
            }
        }

        statement.close();

        return builds;
    }

    public void createBuild(Build build, String ownerId) throws SQLException {
        PreparedStatement statement = connection
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
        statement.setString(2, ownerId);
        statement.setString(3, build.getClassType().toString());

        statement.setString(4, build.getSkill(SkillType.SWORD) != null ? build.getSkill(SkillType.SWORD).toString() : null);
        statement.setInt(5, build.getSkillLevel(SkillType.SWORD));
        statement.setString(6, build.getSkill(SkillType.AXE) != null ? build.getSkill(SkillType.AXE).toString() : null);
        statement.setInt(7, build.getSkillLevel(SkillType.AXE));
        statement.setString(8, build.getSkill(SkillType.BOW) != null ? build.getSkill(SkillType.BOW).toString() : null);
        statement.setInt(9, build.getSkillLevel(SkillType.BOW));
        statement.setString(10, build.getSkill(SkillType.PASSIVE_A) != null ? build.getSkill(SkillType.PASSIVE_A).toString() : null);
        statement.setInt(11, build.getSkillLevel(SkillType.PASSIVE_A));
        statement.setString(12, build.getSkill(SkillType.PASSIVE_B) != null ? build.getSkill(SkillType.PASSIVE_B).toString() : null);
        statement.setInt(13, build.getSkillLevel(SkillType.PASSIVE_B));
        statement.setString(14, build.getSkill(SkillType.PASSIVE_C) != null ? build.getSkill(SkillType.PASSIVE_C).toString() : null);
        statement.setInt(15, build.getSkillLevel(SkillType.PASSIVE_C));
        statement.setString(16, build.getSkill(SkillType.CLASS_PASSIVE) != null ? build.getSkill(SkillType.CLASS_PASSIVE).toString() : null);
        statement.setInt(17, build.getSkillPoints());

        statement.executeUpdate();

        statement.close();
    }

    public void updateBuild(Build build) throws SQLException {
        //Prepare the sql statement
        PreparedStatement statement = connection
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
        statement.setString(1, build.getSkill(SkillType.SWORD) != null ? build.getSkill(SkillType.SWORD).toString() : null);
        statement.setInt(2, build.getSkillLevel(SkillType.SWORD));
        statement.setString(3, build.getSkill(SkillType.AXE) != null ? build.getSkill(SkillType.AXE).toString() : null);
        statement.setInt(4, build.getSkillLevel(SkillType.AXE));
        statement.setString(5, build.getSkill(SkillType.BOW) != null ? build.getSkill(SkillType.BOW).toString() : null);
        statement.setInt(6, build.getSkillLevel(SkillType.BOW));
        statement.setString(7, build.getSkill(SkillType.PASSIVE_A) != null ? build.getSkill(SkillType.PASSIVE_A).toString() : null);
        statement.setInt(8, build.getSkillLevel(SkillType.PASSIVE_A));
        statement.setString(9, build.getSkill(SkillType.PASSIVE_B) != null ? build.getSkill(SkillType.PASSIVE_B).toString() : null);
        statement.setInt(10, build.getSkillLevel(SkillType.PASSIVE_B));
        statement.setString(11, build.getSkill(SkillType.PASSIVE_C) != null ? build.getSkill(SkillType.PASSIVE_C).toString() : null);
        statement.setInt(12, build.getSkillLevel(SkillType.PASSIVE_C));
        statement.setInt(13, build.getSkillPoints());
        statement.setString(14, build.getId());


        statement.executeUpdate();

        statement.close();

    }

    public void deleteBuild(String id) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("DELETE FROM build WHERE id = ?");
        statement.setString(1, id);
        statement.executeUpdate();
        statement.close();
    }
}
