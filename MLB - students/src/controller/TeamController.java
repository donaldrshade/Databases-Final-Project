package controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import view.TeamView;
import bo.Team;
import bo.TeamSeason;
import dataaccesslayer.HibernateUtil;

public class TeamController extends BaseController {

    @Override
    public void init(String query) {
        System.out.println("building dynamic html for team");
        view = new TeamView();
        process(query);
    }
    @Override
    protected void performAction() {
        String action = keyVals.get("action");
        System.out.println("teamcontroller performing action: " + action);
        if (action.equalsIgnoreCase(ACT_SEARCHFORM)) {
            processSearchForm();
        } else if (action.equalsIgnoreCase(ACT_SEARCH)) {
            processSearch();
        } else if (action.equalsIgnoreCase(ACT_DETAIL)) {
            processDetails();
        } else if (action.equalsIgnoreCase(ACT_ROSTER)){
            generateRosterTable();
        }
    }

    protected void processSearchForm() {
        view.buildSearchForm();
    }
    
    protected final void processSearch() {
        String name = keyVals.get("name");
        if (name == null) {
            return;
        }
        String v = keyVals.get("exact");
        boolean exact = (v != null && v.equalsIgnoreCase("on"));
        List<Team> bos = HibernateUtil.retrieveTeamsByName(name, exact);
        view.printSearchResultsMessage(name, exact);
        buildSearchResultsTableTeam(bos);
        view.buildLinkToSearch();
    }

    protected final void processDetails() {
        String id = keyVals.get("id");
        if (id == null) {
            return;
        }
        Team t = (Team) HibernateUtil.retrieveTeamById(Integer.valueOf(id));
        if (t == null) return;
        buildSearchResultsTableTeamDetail(t);
        view.buildLinkToSearch();
    }
    public final void generateRosterTable() {
        Integer tid = Integer.valueOf(keyVals.get("tid"));
        Integer year = Integer.valueOf(keyVals.get("yid"));
        if (tid == null || year  == null) {
            return;
        }
        Team t = (Team) HibernateUtil.retrieveTeamById(Integer.valueOf(tid));
        if (t == null) return;
        String[][] headerTable = new String[2][4];
        headerTable[0][0] = "Name";
        headerTable[0][1] = "League";
        headerTable[0][2] = "Year Founded";
        headerTable[0][3] = "Player Payroll";
        headerTable[1][0] = t.getName();
        headerTable[1][1] = t.getLeague();
        headerTable[1][2] = t.getYearFounded().toString();
        
        bo.Player[] p = HibernateUtil.retrievePlayersByTeamYear(tid, year);
        if(p==null) return;
        String[][] playerTable = new String[p.length+1][3];
        playerTable[0][0] = "Name";
        playerTable[0][1] = "Games Played";
        playerTable[0][2] = "Salary";
        Double totalSal = 0.00;
        for(int i = 1;i<p.length+1;i++){
            playerTable[i][0] = "<a href=player.ssp?id="+p[i-1].getId()+"&action=details>"+p[i-1].getName()+"</a>";
            playerTable[i][1] = p[i-1].getPlayerSeason(year).getGamesPlayed().toString();
            double sal = p[i-1].getPlayerSeason(year).getSalary();
            playerTable[i][2] = DOLLAR_FORMAT.format(sal);
            totalSal+=sal;
        }
        headerTable[1][3] = DOLLAR_FORMAT.format(totalSal);
        view.buildTable(headerTable);
        view.buildTable(playerTable);
    }

    private void buildSearchResultsTableTeam(List<Team> bos) {
        // need a row for the table headers
        String[][] table = new String[bos.size() + 1][5];
        table[0][0] = "Id";
        table[0][1] = "Name";
        table[0][2] = "League";
        table[0][3] = "Year Founded";
        table[0][4] = "Most Recent Year";
        for (int i = 0; i < bos.size(); i++) {
            Team t = bos.get(i);
            String tid = t.getId().toString();
            table[i + 1][0] = view.encodeLink(new String[]{"id"}, new String[]{tid}, tid, ACT_DETAIL, "team");
            table[i + 1][1] = t.getName();
            table[i + 1][2] = t.getLeague();
            table[i + 1][3] = t.getYearFounded().toString();
            table[i + 1][4] = t.getYearLast().toString();
        }
        view.buildTable(table);
    }
    
    private void buildSearchResultsTableTeamDetail(Team t) {
    	Set<TeamSeason> seasons = t.getSeasons();
    	//Set<String> positions = t.getPositions();
    	List<TeamSeason> list = new ArrayList<TeamSeason>(seasons);
    	Collections.sort(list, TeamSeason.teamSeasonsComparator);
        
        // build 2 tables.  first the player details, then the season details
        // need a row for the table headers
        String[][] teamTable = new String[2][4];
        teamTable[0][0] = "Name";
        teamTable[0][1] = "League";
        teamTable[0][2] = "Year Founded";
        teamTable[0][3] = "Most Recent Year";
        
        teamTable[1][0] = t.getName();
        teamTable[1][1] = t.getLeague();
        teamTable[1][2] = t.getYearFounded().toString();
        teamTable[1][3] = t.getYearLast().toString();
        
        view.buildTable(teamTable);
        // now for seasons
        String[][] seasonTable = new String[seasons.size()+1][7];
        seasonTable[0][0] = "Year";
        seasonTable[0][1] = "Games Played";
        seasonTable[0][2] = "Roster";
        seasonTable[0][3] = "Wins";
        seasonTable[0][4] = "Losses";
        seasonTable[0][5] = "Rank";
        seasonTable[0][6] = "Attendance";
        int i = 0;
        for (TeamSeason ts: list) {
        	i++;
        	seasonTable[i][0] = ts.getYear().toString();
        	seasonTable[i][1] = ts.getGamesPlayed().toString();
            seasonTable[i][2] = "<a href=team.ssp?tid="+t.getId()+"&yid="+ts.getYear()+"&action=roster>Roster</a>";
        	seasonTable[i][3] = ts.getWins().toString();
        	seasonTable[i][4] = ts.getLosses().toString();
        	seasonTable[i][5] = ts.getRank().toString();
        	seasonTable[i][6] = ts.getTotalAttendance().toString();
        }
        view.buildTable(seasonTable);
    }

}
