package com.ne.gs.dataholders;

import com.ne.gs.model.templates.custom_quests.CustomQuestTemplate;
import com.ne.gs.services.custom.CustomQuestsService;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collection;
import java.util.List;

/**
 * @author ViAl
 */
@XmlRootElement(name = "custom_quests")
public class CustomQuestsData {

    @XmlElement(name = "custom_quest")
    private List<CustomQuestTemplate> customQuests;


    public Collection<CustomQuestTemplate> getTemplates() {
        return customQuests;
    }

    /**
     * @param questId
     * @return CustomQuestTemplate if there is any quests with this id active, or null
     */
    public CustomQuestTemplate getTemplate(int questId) {
        for (CustomQuestTemplate qt : customQuests)
            if (qt.getId() == questId && CustomQuestsService.getInstance().isActive(qt))
                return qt;
        return null;
    }

    public int size() {
        return customQuests.size();
    }

}

