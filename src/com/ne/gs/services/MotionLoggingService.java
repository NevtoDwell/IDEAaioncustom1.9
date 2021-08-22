/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import javolution.util.FastMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.commons.database.DatabaseFactory;
import com.ne.gs.dataholders.MotionData;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.templates.item.WeaponType;
import com.ne.gs.skillengine.model.Motion;
import com.ne.gs.skillengine.model.MotionTime;
import com.ne.gs.skillengine.model.SkillTemplate;
import com.ne.gs.skillengine.model.WeaponTypeWrapper;

/**
 * @author kecimis
 */
public class MotionLoggingService {

    private static final Logger log = LoggerFactory.getLogger(MotionLoggingService.class);

    private final FastMap<String, MotionLog> motionsMap = new FastMap<String, MotionLog>().shared();

    private boolean advancedLog = false;

    private boolean started = false;

    public static MotionLoggingService getInstance() {
        return SingletonHolder.instance;
    }

    public void start() {
        if (started) {
            return;
        }
        started = true;
        // load data from sql
        loadFromSql();
    }

    public void logTime(Player player, SkillTemplate sk, int clientTime, double distance) {
        if (!started) {
            return;
        }
        if (sk == null) {
            return;
        }
        if (player.getEquipment().getMainHandWeaponType() == null) {
            return;
        }
        Motion motion = sk.getMotion();
        if (motion == null) {
            return;
        }

        int currentAttackSpeed = player.getGameStats().getAttackSpeed().getCurrent();
        int skillId = sk.getSkillId();
        WeaponType mainHandWeapon = player.getEquipment().getMainHandWeaponType();
        WeaponType offHandWeapon = player.getEquipment().getOffHandWeaponType();
        String motionName = motion.getName();
        // clientTime is send from client
        int baseTime = clientTime;// adjusted time

        if (motion.getInstantSkill()) {
            player.sendMsg("Skill: " + skillId + " is instant");
            return;
        } else if (clientTime == 0) {
            player.sendMsg("ClientTime is 0 for skill: " + skillId);
            return;
        }

        long ammoTime = 0;
        if (sk.getAmmoSpeed() != 0) {
            ammoTime = Math.round(distance / sk.getAmmoSpeed() * 1000);// checked with client
        }

        // adjusting with ammospeed
        baseTime -= ammoTime;

        // adjust clientTime if play speed is not 100
        if (motion.getSpeed() != 100) {
            baseTime /= motion.getSpeed();
            baseTime *= 100;
        }

        // logging
        if (advancedLog) {
            player.sendMsg("skillId: " + sk.getSkillId() + " motionName: " + motionName);
            player.sendMsg("attackSpeed: " + currentAttackSpeed + " mainHand: " + mainHandWeapon.toString() + " isDual: "
                + (offHandWeapon != null));
            player.sendMsg("clientTime: " + clientTime + " baseTime: " + baseTime + " playSpeed: " + motion.getSpeed());
            player.sendMsg("ammoTime: " + ammoTime + " ammoSpeed: " + sk.getAmmoSpeed() + " distance: " + distance);
            player.sendMsg("-------------------");
        } else {
            player.sendMsg("motionName: " + motionName + " clientTime: " + clientTime + " baseTime: " + baseTime);
        }

        // create WeaponTypeWrapper
        WeaponTypeWrapper weapon = new WeaponTypeWrapper(mainHandWeapon, offHandWeapon);
        // check if its present
        if (isPresent(motionName, weapon, skillId, currentAttackSpeed)) {
            log.info("motionName: " + motionName + " weapon: " + (offHandWeapon != null ? "dual" : mainHandWeapon.toString()) + " skillId: " + skillId
                + " currentAttackSpeed: " + currentAttackSpeed + "baseTime: " + baseTime + " storedTime: "
                + getTime(motionName, weapon, skillId, currentAttackSpeed));
            player.sendMsg("Its already stored. storedTime: " + getTime(motionName, weapon, skillId, currentAttackSpeed));
            return;
        }

        // addtime
        if (addTime(motionName, weapon, skillId, currentAttackSpeed, baseTime)) {
            player.sendMsg("BaseTime: " + baseTime + " for motion: " + motionName + " was added.");
        } else {
            player.sendMsg("Couldnt add baseTime: " + baseTime + " for motion: " + motionName + "!");
        }

    }

    public void createAnalyzeFiles() {
        TreeMap<String, WeaponTime> resultsBase = new TreeMap<>();
        TreeMap<String, WeaponTime> resultsCap = new TreeMap<>();
        TreeMap<String, WeaponTime> results1000 = new TreeMap<>();
        for (Entry<String, MotionLog> entry : motionsMap.entrySet()) {
            String fileName = "motions/" + entry.getKey() + ".xml";
            WeaponTime weaponTimeBase = new WeaponTime();
            WeaponTime weaponTimeCap = new WeaponTime();
            WeaponTime weaponTime1000 = new WeaponTime();
            try {
                FileWriter fstream = new FileWriter(fileName);
                BufferedWriter out = new BufferedWriter(fstream);

                out.append("<motion name=\"" + entry.getKey() + "\">\n");

                if (entry.getValue() != null) {
                    // loop through weaponType
                    for (Entry<WeaponTypeWrapper, List<SkillTime>> entry2 : entry.getValue().getMotionLog().entrySet()) {
                        WeaponTypeWrapper weapon = entry2.getKey();
                        out.append("\t<motionlog " + weapon.toString() + "\">\n");

                        if (entry2.getValue() == null) {
                            continue;
                        }
                        for (SkillTime st : entry2.getValue()) {
                            out.append("\t\t<skilltime skillId=\"" + st.getSkillId() + "\" attackspeed=\"" + st.getAttackSpeed() + "\" clienttime=\""
                                + st.getClientTime() + "\">\n");
                            // fill weaponTime
                            weaponTimeBase.add(weapon, recalculate("base", weapon, st.getAttackSpeed(), st.getClientTime()));
                            weaponTimeCap.add(weapon, recalculate("cap", weapon, st.getAttackSpeed(), st.getClientTime()));
                            weaponTime1000.add(weapon, recalculate("1000", weapon, st.getAttackSpeed(), st.getClientTime()));

                        }
                        out.append("\t<motionlog/>\n");
                    }

                    // fill results
                    resultsBase.put(entry.getKey(), weaponTimeBase);
                    resultsCap.put(entry.getKey(), weaponTimeCap);
                    results1000.put(entry.getKey(), weaponTime1000);
                }
                out.append("<motion/>");
                out.close();
                fstream.close();
            } catch (IOException e) {
                log.error("Error: " + e.getMessage());
            }
        }

        // create results files
        String fileName = "motions/results.xml";
        try {
            FileWriter fstream = new FileWriter(fileName);
            BufferedWriter out = new BufferedWriter(fstream);
            out.append("<base attack speed>\n");
            for (Entry<String, WeaponTime> entry : resultsBase.entrySet()) {
                out.append("<motion name=\"" + entry.getKey() + "\">\n");
                TreeMap<WeaponTypeWrapper, Integer> map = entry.getValue().process();

                for (Entry<WeaponTypeWrapper, Integer> entry2 : map.entrySet()) {
                    out.append("\t<weapontime " + entry2.getKey().toString() + "\" time=\"" + entry2.getValue() + "\" >\n");
                }
                out.append("<motion/>\n");
                map.clear();
            }
            out.append("<base attack speed/>\n\n");

            out.append("<cap attack speed>\n");
            for (Entry<String, WeaponTime> entry : resultsCap.entrySet()) {
                out.append("<motion name=\"" + entry.getKey() + "\">\n");
                TreeMap<WeaponTypeWrapper, Integer> map = entry.getValue().process();

                for (Entry<WeaponTypeWrapper, Integer> entry2 : map.entrySet()) {
                    out.append("\t<weapontime " + entry2.getKey().toString() + "\" time=\"" + entry2.getValue() + "\" >\n");
                }
                out.append("<motion/>\n");
                map.clear();
            }
            out.append("<cap attack speed/>\n\n");

            out.append("<1000 attack speed>\n");
            for (Entry<String, WeaponTime> entry : results1000.entrySet()) {
                out.append("<motion name=\"" + entry.getKey() + "\">\n");
                TreeMap<WeaponTypeWrapper, Integer> map = entry.getValue().process();

                for (Entry<WeaponTypeWrapper, Integer> entry2 : map.entrySet()) {
                    out.append("\t<weapontime " + entry2.getKey().toString() + "\" time=\"" + entry2.getValue() + "\" >\n");
                }
                out.append("<motion/>\n");
                map.clear();
            }
            out.append("<1000 attack speed/>\n");

            out.close();
            fstream.close();
        } catch (IOException e) {
            log.error("Error: " + e.getMessage());
        }

    }

    public void createFinalFile() {
        MotionData motionData = new MotionData();
        List<MotionTime> motionTimes = motionData.getMotionTimes();

        // create results
        TreeMap<String, WeaponTime> results = new TreeMap<>();
        for (Entry<String, MotionLog> entry : motionsMap.entrySet()) {
            WeaponTime weaponTime = new WeaponTime();
            if (entry.getValue() == null) {
                continue;
            }
            // loop through weaponType
            for (Entry<WeaponTypeWrapper, List<SkillTime>> entry2 : entry.getValue().getMotionLog().entrySet()) {
                WeaponTypeWrapper weapon = entry2.getKey();

                if (entry2.getValue() == null) {
                    continue;
                }
                for (SkillTime st : entry2.getValue()) {
                    weaponTime.add(weapon, recalculate("base", weapon, st.getAttackSpeed(), st.getClientTime()));
                }
                // fill results
                results.put(entry.getKey(), weaponTime);
            }
        }

        for (Entry<String, WeaponTime> entry : results.entrySet()) {
            TreeMap<WeaponTypeWrapper, Integer> map = entry.getValue().process();
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            // create time
            Set<WeaponTypeWrapper> listofWeapons = new TreeSet<>();
            // ordering BOOK_2H BOW DAGGER_1H MACE_1H ORB_2H POLEARM_2H STAFF_2H SWORD_1H SWORD_2H dualwield
            listofWeapons.add(new WeaponTypeWrapper(WeaponType.BOOK_2H, null));
            listofWeapons.add(new WeaponTypeWrapper(WeaponType.BOW, null));
            listofWeapons.add(new WeaponTypeWrapper(WeaponType.DAGGER_1H, null));
            listofWeapons.add(new WeaponTypeWrapper(WeaponType.MACE_1H, null));
            listofWeapons.add(new WeaponTypeWrapper(WeaponType.ORB_2H, null));
            listofWeapons.add(new WeaponTypeWrapper(WeaponType.POLEARM_2H, null));
            listofWeapons.add(new WeaponTypeWrapper(WeaponType.STAFF_2H, null));
            listofWeapons.add(new WeaponTypeWrapper(WeaponType.SWORD_1H, null));
            listofWeapons.add(new WeaponTypeWrapper(WeaponType.SWORD_2H, null));
            listofWeapons.add(new WeaponTypeWrapper(WeaponType.SWORD_1H, WeaponType.SWORD_1H));

            for (WeaponTypeWrapper weapon : listofWeapons) {
                if (first) {
                    sb.append((map.containsKey(weapon) ? map.get(weapon) : "0"));
                    first = false;
                } else {
                    sb.append("," + (map.containsKey(weapon) ? map.get(weapon) : "0"));
                }
            }
            // create MotionTime
            MotionTime motion = new MotionTime();
            motion.setName(entry.getKey());
            motion.setTimes(sb.toString());

            motionTimes.add(motion);
        }

        // marshall the final xml file
        marshallFile(motionData, "data/static_data/skills/new_motion_times.xml");
    }

    public static void marshallFile(Object templates, String file) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(templates.getClass());
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(templates, new FileOutputStream(file));
        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * method used to recalculate time to base, cap or given attackspeed
     *
     * @param method
     * @param weapon
     * @param attackSpeed
     * @param time
     *
     * @return
     */
    private int recalculate(String method, WeaponTypeWrapper weapon, int attackSpeed, int time) {
        int finalTime = 0;

        TreeMap<WeaponType, float[]> list = new TreeMap<>();

        float dagger[] = {600f, 1200f};
        list.put(WeaponType.DAGGER_1H, dagger);
        float mace[] = {750f, 1500f};
        list.put(WeaponType.MACE_1H, mace);
        float sword1h[] = {700f, 1400f};
        list.put(WeaponType.SWORD_1H, sword1h);
        float bookorb[] = {1100f, 2200f};
        list.put(WeaponType.BOOK_2H, bookorb);
        list.put(WeaponType.ORB_2H, bookorb);
        float polearm[] = {1400f, 2800f};
        list.put(WeaponType.POLEARM_2H, polearm);
        float staff[] = {1000f, 2000f};
        list.put(WeaponType.STAFF_2H, staff);
        float swordbow[] = {1200f, 2400f};
        list.put(WeaponType.SWORD_2H, swordbow);
        list.put(WeaponType.BOW, swordbow);

        float speed = 0;
        if (method.equalsIgnoreCase("base")) {
            speed = list.get(weapon.getMainHand())[1];
            if (weapon.getOffHand() != null) {
                speed += (list.get(weapon.getOffHand())[1] * 0.25);
            }
        } else if (method.equalsIgnoreCase("cap")) {
            speed = list.get(weapon.getMainHand())[0];
            if (weapon.getOffHand() != null) {
                speed += (list.get(weapon.getOffHand())[0] * 0.25);
            }
        } else {
            try {
                speed = Float.parseFloat(method);
            } catch (Exception e) {
                // log
            }
        }

        finalTime = Math.round((float) time / (float) attackSpeed * speed);

        return finalTime;
    }

    // save to sql
    public void saveToSql() {
        Connection con = null;

        String INSERT_QUERY = "INSERT INTO skill_motions (motion_name, weapon_type, off_weapon_type, skill_id, attack_speed, time) VALUES (?,?,?,?,?,?) ON DUPLICATE KEY UPDATE motion_name = ?";

        try {
            con = DatabaseFactory.getConnection();
            PreparedStatement stmt = con.prepareStatement(INSERT_QUERY);
            for (Entry<String, MotionLog> entry : motionsMap.entrySet()) {
                String motionName = entry.getKey();
                // set motion_name
                stmt.setString(1, motionName);
                stmt.setString(7, motionName);
                if (entry.getValue() == null) {
                    continue;
                }
                // loop through weaponType
                for (Entry<WeaponTypeWrapper, List<SkillTime>> entry2 : entry.getValue().getMotionLog().entrySet()) {
                    String weaponType = (entry2.getKey().getMainHand() != null ? entry2.getKey().getMainHand().toString() : "null");
                    String offWeaponType = (entry2.getKey().getOffHand() != null ? entry2.getKey().getOffHand().toString() : "null");
                    // set weapon_type
                    stmt.setString(2, weaponType);
                    stmt.setString(3, offWeaponType);

                    if (entry2.getValue() == null) {
                        continue;
                    }
                    // sort by skillId
                    Collections.sort(entry2.getValue());
                    for (SkillTime st : entry2.getValue()) {
                        // set skill_id
                        stmt.setInt(4, st.getSkillId());
                        // set attack_speed
                        stmt.setInt(5, st.getAttackSpeed());
                        // set time
                        stmt.setInt(6, st.getClientTime());
                        stmt.execute();
                    }
                }
            }
            stmt.close();
        } catch (SQLException e) {
            log.error("MotionLoggingService", e);
        } finally {
            DatabaseFactory.close(con);
        }
    }

    // load from sql
    public void loadFromSql() {
        Connection con = null;

        String SELECT_QUERY = "SELECT * FROM skill_motions";
        try {
            con = DatabaseFactory.getConnection();
            PreparedStatement stmt = con.prepareStatement(SELECT_QUERY);

            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                String motionName = resultSet.getString("motion_name");
                WeaponType mainHandWeapon = WeaponType.valueOf(resultSet.getString("weapon_type"));
                WeaponType offHandWeapon = (resultSet.getString("off_weapon_type").contains("null") ? null : WeaponType.valueOf(resultSet
                    .getString("off_weapon_type")));
                int skillId = resultSet.getInt("skill_id");
                int attackSpeed = resultSet.getInt("attack_speed");
                int time = resultSet.getInt("time");
                WeaponTypeWrapper weapon = new WeaponTypeWrapper(mainHandWeapon, offHandWeapon);

                addTime(motionName, weapon, skillId, attackSpeed, time);
            }
            resultSet.close();
            stmt.close();
        } catch (SQLException e) {
            log.error("MotionLoggingService", e);
        } finally {
            DatabaseFactory.close(con);
        }
    }

    private boolean isPresent(String motionName, WeaponTypeWrapper weapon, int skillId, int currentAttackSpeed) {
        if (motionsMap.containsKey(motionName)) {
            return motionsMap.get(motionName).isPresent(weapon, skillId, currentAttackSpeed);
        }

        return false;
    }

    private int getTime(String motionName, WeaponTypeWrapper weapon, int skillId, int currentAttackSpeed) {
        if (motionsMap.containsKey(motionName)) {
            return motionsMap.get(motionName).getTime(weapon, skillId, currentAttackSpeed);
        }

        return 0;
    }

    public boolean addTime(String motionName, WeaponTypeWrapper weapon, int skillId, int currentAttackSpeed, int clientTime) {
        if (!motionsMap.containsKey(motionName)) {
            MotionLog motionLog = new MotionLog();
            boolean result = motionLog.addSkillTime(weapon, new SkillTime(skillId, currentAttackSpeed, clientTime));
            motionsMap.put(motionName, motionLog);
            return result;
        } else {
            return motionsMap.get(motionName).addSkillTime(weapon, new SkillTime(skillId, currentAttackSpeed, clientTime));
        }
    }

    public void setAdvancedLog(boolean bol) {
        advancedLog = bol;
    }

    public boolean getAdvancedLog() {
        return advancedLog;
    }

    private MotionLoggingService() {
        log.info("MotionLoggingService started.");
    }

    @SuppressWarnings("synthetic-access")
    private static final class SingletonHolder {

        protected static final MotionLoggingService instance = new MotionLoggingService();
    }

    private class MotionLog {

        private final FastMap<WeaponTypeWrapper, List<SkillTime>> motionsForWeapons = new FastMap<>();

        public FastMap<WeaponTypeWrapper, List<SkillTime>> getMotionLog() {
            return motionsForWeapons;
        }

        public boolean addSkillTime(WeaponTypeWrapper weapon, SkillTime skillTime) {
            if (motionsForWeapons.containsKey(weapon)) {
                if (!motionsForWeapons.containsValue(skillTime)) {
                    motionsForWeapons.get(weapon).add(skillTime);
                    return true;
                }
            } else {
                List<SkillTime> list = new ArrayList<>();
                list.add(skillTime);
                motionsForWeapons.put(weapon, list);
                return true;
            }

            return false;
        }

        public int getTime(WeaponTypeWrapper weapon, int skillId, int currentAttackSpeed) {
            if (motionsForWeapons.containsKey(weapon)) {
                for (SkillTime st : motionsForWeapons.get(weapon)) {
                    if (st.getSkillId() == skillId && st.getAttackSpeed() == currentAttackSpeed) {
                        return st.getClientTime();
                    }
                }
            }

            return 0;
        }

        public boolean isPresent(WeaponTypeWrapper weapon, int skillId, int currentAttackSpeed) {
            if (motionsForWeapons.containsKey(weapon)) {
                for (SkillTime st : motionsForWeapons.get(weapon)) {
                    if (st.getSkillId() == skillId && st.getAttackSpeed() == currentAttackSpeed) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    private class SkillTime implements Comparable<SkillTime> {

        private final int skillId;
        private final int attackSpeed;
        private final int clientTime;

        public SkillTime(int skillId, int attackSpeed, int clientTime) {
            this.skillId = skillId;
            this.attackSpeed = attackSpeed;
            this.clientTime = clientTime;
        }

        @Override
        public int compareTo(SkillTime o) {
            if (skillId < o.getSkillId()) {
                return -1;
            } else if (skillId > o.getSkillId()) {
                return 1;
            } else {
                return 0;
            }
        }

        /*
         * (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + clientTime;
            result = prime * result + attackSpeed;
            result = prime * result + skillId;
            return result;
        }

        /*
         * (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            SkillTime other = (SkillTime) obj;
            if (!getOuterType().equals(other.getOuterType())) {
                return false;
            }
            if (attackSpeed != other.attackSpeed) {
                return false;
            }
            if (skillId != other.skillId) {
                return false;
            }
            return true;
        }

        public int getSkillId() {
            return skillId;
        }

        public int getAttackSpeed() {
            return attackSpeed;
        }

        public int getClientTime() {
            return clientTime;
        }

        private MotionLoggingService getOuterType() {
            return MotionLoggingService.this;
        }
    }

    private class WeaponTime {

        private final TreeMap<WeaponTypeWrapper, List<Integer>> values = new TreeMap<>();

        public void add(WeaponTypeWrapper weapon, int value) {
            if (values.containsKey(weapon)) {
                values.get(weapon).add(value);
            } else {
                List<Integer> list = new ArrayList<>();
                list.add(value);
                values.put(weapon, list);
            }
        }

        public TreeMap<WeaponTypeWrapper, Integer> process() {
            TreeMap<WeaponTypeWrapper, Integer> weaponMap = new TreeMap<>();

            for (Entry<WeaponTypeWrapper, List<Integer>> entry2 : values.entrySet()) {
                // logic to calculate one value per weaponType
                // count the element with the most occurencies
                int finalValue = 0;
                int maxFrequency = 0;
                int value = 0;
                int total = 0;
                for (Integer i : entry2.getValue()) {
                    total += i;
                    if (calculateFrequency(entry2.getValue(), i) > maxFrequency) {
                        maxFrequency = calculateFrequency(entry2.getValue(), i);
                        value = i;
                    }
                }
                log.info("maxFrequency: " + maxFrequency + " value: " + value + " size: " + entry2.getValue().size());
                // if frequency of given value is higher than 70% take it, otherwise do Arithmetic mean
                if (Math.round(entry2.getValue().size() * 0.7f) <= maxFrequency) {
                    finalValue = value;
                } else {
                    finalValue = total / entry2.getValue().size();
                }

                log.info("weaponTime.process() finalValue: " + finalValue);
                weaponMap.put(entry2.getKey(), finalValue);
            }

            return weaponMap;
        }
    }

    private int calculateFrequency(List<Integer> list, int value) {
        int frequency = 0;

        // 10% tolerance
        int min = Math.round(value * 0.90f);
        int max = Math.round(value * 1.1f);
        for (Integer i : list) {
            if (i == null) {
                continue;
            }
            if (min <= value && max >= value) {
                frequency++;
            }
        }

        return frequency;
    }
}
