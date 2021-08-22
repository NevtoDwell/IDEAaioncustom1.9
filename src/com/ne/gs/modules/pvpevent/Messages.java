package com.ne.gs.modules.pvpevent;

import com.ne.commons.utils.L10N;

/**
 * @author hex1r0
 */
public enum Messages implements L10N.Translatable {
    REGISTER_OPEN("Регистрация на %s открыта на %s. Введите в чат '.event apply %s' для регистрации"),
    REGISTER_SUCCESS("Вы успешно зарегистрировались на ивент %s [%d/%d]. Не посещайте данжи и не используйте оружие, иначе регистрация будет отменена!"),
    REGISTER_PARTICIPANT("Игрок %s зарегистрировался на ивент %s [%d/%d]"),
    REGISTER_WRONG_TIME("В данный момент регистрация на ивент недоступна"),
    REGISTER_NOT_ENOUGH_PLS("Не найдено достаточное ко-во противников. Регистрация отменена"),
    REGISTER_TIME_OVER("Время регистрации закончено"),
    REGISTER_NO_EVENTS("В настоящее время нет доступных ивентов или введена неверная команда"),
    REGISTER_COOLDOWN("Вам нужно подождать %s для регистрации"),
    REGISTER_INVALID_STATE("Нельзя находиться в боевом режиме или данже!"),
    REGISTER_INVALID_CMD("Неверно введена команда"),
    REGISTER_INVALID_LEVEL("Неправильный уровень. Необходим %s"),
    CANCEL_SUCCESS("Вы успешно отменили регистрацию на ивент %s"),
    REGISTER_TWICE_ERROR("Вы уже зарегистрированы на ивент %s. Для регистрации на другой ивент, пожалуйста отмените предыдущий"),
    EVENT_IN_PROGRESS("Не доступно во время работы ивента"),
    ROUND_STARTS_IN("Раунд #%d начнется через %s"),
    ROUND_STARTED("Раунд #%d начат"),
    ROUND_FINISHED("Раунд #%d завершен"),
    PLAYER_LEAVE_MASS_EVENT("Игрок - %s покидает ивент %s"),
    ROUND_FINISHED_WINNER("Раунд #%d завершен. Победитель - %s"),
    ROUND_FINISHED_NOT_WINNER("Раунд #%d завершен. Победителя нет"),
    EVENT_FINISHED("Ивент %s окончен"),
    PLAYER_LEAVE_EVENT("Ивент %s окончен, ваш соперник покинул ивент."),
    POLL_EVENT("Ивент <font color=\"D4FF54\">%s</font>"),
    POLL_EVENT_REWARD("Награда за Ивент:"),
    POLL_EVENT_REWARD_AP("Вы получили <font color=\"D4FF54\">%d</font> очки бездны"),
    POLL_EVENT_REWARD_LVL("Вы получили <font color=\"D4FF54\">%d</font> уровень"),
    POLL_EVENT_REWARD_GP("Вы получили <font color=\"D4FF54\">%d</font> донат монет"),
    POLL_EVENT_REWARD_RND_ITEM("Вы получили <font color=\"D4FF54\">%d</font> случайный предмет"),
    CHOOSE_ITEM("Выберите или получите <font color=\"D4FF54\">%d</font> предмет"),;

    private final String _defaultValue;

    private Messages(String defaultValue) {
        _defaultValue = defaultValue;
    }

    @Override
    public String id() {
        return toString();
    }

    @Override
    public String defaultValue() {
        return _defaultValue;
    }
}
