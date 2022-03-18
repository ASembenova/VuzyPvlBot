package kz.saa.vuzy_pvl_bot.telegram;

public enum BotState {
    START,
    MAIN_MENU,
    SELECT_ALL,
    SEARCH,
    TYPE_SEARCH_QUERY,
    COMPARE, COMPARE_BYNAME, COMPARE_BYCODE, COMPARE_BYNAME_AND_CODE,
    SELECT_ONE, /*THIS_ONE_SELECTED,*/
    HELP, SELECT_LANG
}
