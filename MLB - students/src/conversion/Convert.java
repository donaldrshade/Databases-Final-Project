package conversion;

/*import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

import java.util.List;

import java.sql.SQLException;

import bo.BattingStats;
import bo.CatchingStats;
import bo.FieldingStats;
import bo.PitchingStats;
import bo.Player;
import bo.PlayerSeason;
import bo.Team;
import bo.TeamSeason;
import dataaccesslayer.HibernateUtil;//*/

public class Convert {
	/*
	static Connection conn;
	static final String MYSQL_CONN_URL = "jdbc:mysql://163.11.238.237/mlb?"
	//TO SEE DATABASE GOTO 163.11.238.237/phpmyadmin
    + "verifyServerCertificate=false&useSSL=true&" // PPD
    + "user=shade&password=password"; 

	public static void main(String[] args) {
		try {
			long startTime = System.currentTimeMillis();
			conn = DriverManager.getConnection(MYSQL_CONN_URL);
			
			//convertPlayers();
			convertTeams();
			long endTime = System.currentTimeMillis();
			long elapsed = (endTime - startTime) / (1000*60);
			System.out.println("Elapsed time in mins: " + elapsed);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (!conn.isClosed()) conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		HibernateUtil.stopConnectionProvider(); //PPD
		HibernateUtil.getSessionFactory().close();
	}
	public static void convertTeams(){
		//This will be the grab.
		try {
			PreparedStatement ps = conn.prepareStatement("select distinct franchID from Teams "
				//+"where franchID = 'ATL'");
				);

			ResultSet franchID = ps.executeQuery();
			int teams = 0;
			while (franchID.next()) {
				Team t = new Team();
				teams++;
				//get start date for each franchID
				String tid = franchID.getString("franchID");
				PreparedStatement ps2 = conn.prepareStatement("select yearID from Teams where franchID = ? order by yearID asc limit 1");
				ps2.setString(1, tid); 
				ResultSet  rs2 = ps2.executeQuery();  //get the start years for each franchise
				rs2.next();
				t.setYearFounded(Integer.parseInt(rs2.getString("yearID")));
				//get most recent name
				PreparedStatement ps3 = conn.prepareStatement("select name from Teams where franchID = ? order by yearID desc limit 1");
				ps3.setString(1, tid); 
				ResultSet  rs3 = ps3.executeQuery();  //get the recent name for each team
				rs3.next();
				t.setName(rs3.getString("name"));
				//get league
				PreparedStatement ps4 = conn.prepareStatement("select lgID from Teams where franchID = ? order by yearID desc limit 1");
				ps4.setString(1, tid); 
				ResultSet  rs4 = ps4.executeQuery();  //get the league id for each franchise
				rs4.next();
				t.setLeague(rs4.getString("lgID"));
				//check if active or not (if not then get end date)
				PreparedStatement ps5 = conn.prepareStatement("select active from TeamsFranchises where franchID = ?");
				ps5.setString(1, tid); 
				ResultSet  rs5 = ps5.executeQuery();  //get active status of each franchise
				rs5.next();
				String active = rs5.getString("active");
				if(!active.equals("Y")){
					PreparedStatement ps6 = conn.prepareStatement("select yearID from Teams where franchID = ? order by yearID desc limit 1");
					ps6.setString(1, tid); 
					ResultSet  rs6 = ps6.executeQuery();  //get the recent name for each team
					rs6.next();
					t.setYearLast(Integer.parseInt(rs6.getString("yearID")));
				}else{
					t.setYearLast(2016);
				}
				addSeasons(t, tid);
				HibernateUtil.persistTeam(t);
				if(teams % 100 == 0){
					System.out.println("Teams completed: "+teams);
				}
			}
			franchID.close();
			ps.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
		
	public static void convertPlayers() {
		try {
			PreparedStatement ps = conn.prepareStatement("select " + 
				"playerID, " + 
				"nameFirst, " + 
				"nameLast, " + 
				"nameGiven, "+ 
				"birthDay, " + 
				"birthMonth, " + 
				"birthYear, " + 
				"deathDay, "+ 
				"deathMonth, " + 
				"deathYear, " + 
				"bats, " + 
				"throws, " + 
				"birthCity, " + 
				"birthState, " + 
	            "birthCountry, " +
				"debut, " + 
				"finalGame " +
				//"from Master");
				// for debugging comment previous line, uncomment next line
				//"from Master where playerID = 'alvarjo01' or playerID = 'bedrost01';");
				//clean up here
				"from Master where throws='S'");
			ResultSet rs = ps.executeQuery();
			int count=0; // for progress feedback only
			while (rs.next()) {
				count++;
				// this just gives us some progress feedback
				if (count % 1000 == 0) System.out.println("num players: " + count);
        
				String pid = rs.getString("playerID");
				String firstName = rs.getString("nameFirst");
				String lastName = rs.getString("nameLast");
				// this check is for data scrubbing
				// don't want to bring anybody over that doesn't have a pid, firstname and lastname
				if (pid == null	|| pid.isEmpty() || 
					firstName == null || firstName.isEmpty() ||
					lastName == null || lastName.isEmpty()) continue;
				Player p = new Player();
				p.setName(firstName + " " + lastName);
				p.setGivenName(rs.getString("nameGiven"));
        
				java.util.Date birthDay = convertIntsToDate(rs.getInt("birthYear"), rs.getInt("birthMonth"), rs.getInt("birthDay"));
		        if (birthDay!=null) p.setBirthDay(birthDay);
				java.util.Date deathDay = convertIntsToDate(rs.getInt("deathYear"), rs.getInt("deathMonth"), rs.getInt("deathDay"));
				if (deathDay!=null) p.setDeathDay(deathDay);
		
				// need to do some data scrubbing for bats and throws columns
				String hand = rs.getString("bats");
				if (hand!=null){
		          if (hand.equalsIgnoreCase("B")){
		            hand = "S";
		          }
		          else if (hand.equalsIgnoreCase(""))
		            hand = null;
				} 
				p.setBattingHand(hand);
		
				// Clean up throwing hand
				hand = rs.getString("throws");
		        if (hand.equalsIgnoreCase("")){
		            hand = null;
				} 
				p.setThrowingHand(hand);
		
				p.setBirthCity(rs.getString("birthCity"));
				p.setBirthState(rs.getString("birthState"));
		        p.setBirthCountry(rs.getString("birthCountry"));
		        
		        // Clean up debut and final game data.
		        try {
		          java.util.Date firstGame = rs.getDate("debut");
		          if (firstGame!=null) p.setFirstGame(firstGame);
		        }
		        catch (SQLException e){
		          // Ignore conversion error - remains null;
		          System.out.println(pid + ": debut invalid format");
		        }
		        try {
		          java.util.Date lastGame = rs.getDate("finalGame");
		          if (lastGame!=null) p.setLastGame(lastGame);
		        }
		        catch (SQLException e){
		          // Ignore conversion error - remains null
		          System.out.println(pid + ": finalGame invalid format");
		        }
        
				addPositions(p, pid);
				// players bio collected, now go after stats
				addSeasons(p, pid);
				// we can now persist player, and the seasons and stats will cascade
				HibernateUtil.persistPlayer(p);
			}
			rs.close();
			ps.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	private static java.util.Date convertIntsToDate(int year, int month, int day) {
		Calendar c = new GregorianCalendar();
		java.util.Date d=null;
		// if year is 0, then date wasn't populated in MySQL database
		if (year!=0) {
			c.set(year, month-1, day);
			d = c.getTime();
		}
		return d;
	}
	private static boolean areDatesSame(Date a, Date b){
		if(a == null || b == null){
			return false;
		}
		else if(a.getMonth() != b.getMonth()){
			return false;
		}
		else if(a.getYear() != b.getYear()){
			return false;
		}
		else if(a.getDay() != b.getDay()){
			return false;
		}
		else{
			return true;
		}
	}
	
	public static void addPositions(Player p, String pid) {
		Set<String> positions = new HashSet<String>();
		try {
			PreparedStatement ps = conn.prepareStatement("select " +
					"distinct pos " +
					"from Fielding " +
					"where playerID = ?;");
			ps.setString(1, pid);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				String pos = rs.getString("pos");
				positions.add(pos);
			}
			rs.close();
			ps.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		p.setPositions(positions);
	}

	public static void addSeasons(Team t, String franchID){
		try {
			PreparedStatement ps = conn.prepareStatement("select " +
				"yearID," +
				"G," +
				"W," +
				"L," +
				"Rank," +
				"attendance " +
				"from Teams " +
				"where franchID = ?");
			ps.setString(1, franchID);
			ResultSet rs  = ps.executeQuery();
			TeamSeason season = null;
			while(rs.next()){
				int year = rs.getInt("yearID");
				season = new TeamSeason(t, year);
				season.setGamesPlayed(rs.getInt("G"));
				season.setYear(year);
				season.setWins(rs.getInt("W"));
				season.setLosses(rs.getInt("L"));
				season.setRank(rs.getInt("Rank"));
				season.setTotalAttendance(rs.getInt("attendance"));
				PreparedStatement players = conn.prepareStatement("CALL findPlayers(?,?)");
				players.setInt(1, year);
				players.setString(2, franchID);
				ResultSet teamPlayers = players.executeQuery();
				while(teamPlayers.next()){
					String first = teamPlayers.getString(1);
					String last = teamPlayers.getString(2);
					List<Player> retplayers = HibernateUtil.retrievePlayersByName(first+" "+last, true);
					if(retplayers.size()>0){
						if(retplayers.size()>1){
							String nameGiven = teamPlayers.getString(3);
							Date birthday = convertIntsToDate(teamPlayers.getInt("birthYear"), teamPlayers.getInt("birthMonth"), teamPlayers.getInt("birthDay"));
							for(int i=0;i<retplayers.size();i++){
								Player tempPlayer = retplayers.get(i);
								Date temp = tempPlayer.getBirthDay();
								if(tempPlayer.getGivenName().equalsIgnoreCase(nameGiven) && areDatesSame(temp, birthday)){//temp.compareTo(birthday)==0){
									season.addPlayers(retplayers.get(i));
								}
							}

						}else{
							season.addPlayers(retplayers.get(0));
						}
					}
					
				}
				teamPlayers.close();
				players.close();
				t.addSeason(season);
			}
		} catch (Exception e) {
		e.printStackTrace();
	}
	}

	public static void addSeasons(Player p, String pid) {
		try {
			PreparedStatement ps = conn.prepareStatement("select " + 
					"yearID, " + 
					"teamID, " +
					"lgId, " +
					"sum(G) as gamesPlayed " + 
					"from Batting " + 
					"where playerID = ? " + 
					"group by yearID, teamID, lgID;");
			ps.setString(1, pid);
			ResultSet rs = ps.executeQuery();
			PlayerSeason s = null;
			while (rs.next()) {
				int yid = rs.getInt("yearID");
				s = p.getPlayerSeason(yid);
				// it is possible to see more than one of these per player if he switched teams
				// set all of these attrs the first time we see this playerseason
				if (s==null) {
					s = new PlayerSeason(p,yid);
					p.addSeason(s);
					s.setGamesPlayed(rs.getInt("gamesPlayed"));
					double salary = getSalary(pid, yid);
					s.setSalary(salary);
					BattingStats batting = getBatting(s,pid,yid);
					s.setBattingStats(batting);
					FieldingStats fielding = getFielding(s,pid,yid);
					s.setFieldingStats(fielding);
					PitchingStats pitching = getPitching(s,pid,yid);
					s.setPitchingStats(pitching);
					CatchingStats catching = getCatching(s,pid,yid);
					s.setCatchingStats(catching);
				// set this the consecutive time(s) so it is the total games played regardless of team	
				} else {
					s.setGamesPlayed(rs.getInt("gamesPlayed")+s.getGamesPlayed());
				}
			}
			rs.close();
			ps.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static double getSalary(String pid, Integer yid) {
		double salary = 0;
		try {
			PreparedStatement ps = conn.prepareStatement("select " + 
					"sum(salary) as salary " + 
					"from Salaries " + 
					"where playerID = ? " + 
					"and yearID = ? ;");
			ps.setString(1, pid);
			ps.setInt(2, yid);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				salary = rs.getDouble("salary");
			}
			rs.close();
			ps.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return salary;
	}

	public static BattingStats getBatting(PlayerSeason psi, String pid, Integer yid) {
		BattingStats s = new BattingStats();
		try {
			PreparedStatement ps = conn.prepareStatement("select "	+ "" +
					"sum(AB) as atBats, " + 
					"sum(H) as hits, " + 
					"sum(2B) as doubles, " + 
					"sum(3B) as triples, " + 
					"sum(HR) as homeRuns, " + 
					"sum(RBI) as runsBattedIn, " + 
					"sum(SO) as strikeouts, " + 
					"sum(BB) as walks, " + 
					"sum(HBP) as hitByPitch, " + 
					"sum(IBB) as intentionalWalks, " + 
					"sum(SB) as steals, " + 
					"sum(CS) as stealsAttempted " + 
					"from Batting " + 
					"where playerID = ? " + 
					"and yearID = ? ;");
			ps.setString(1, pid);
			ps.setInt(2, yid);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				s.setId(psi);
				s.setAtBats(rs.getInt("atBats"));
				s.setHits(rs.getInt("hits"));
				s.setDoubles(rs.getInt("doubles"));
				s.setTriples(rs.getInt("triples"));
				s.setHomeRuns(rs.getInt("homeRuns"));
				s.setRunsBattedIn(rs.getInt("runsBattedIn"));
				s.setStrikeouts(rs.getInt("strikeouts"));
				s.setWalks(rs.getInt("walks"));
				s.setHitByPitch(rs.getInt("hitByPitch"));
				s.setIntentionalWalks(rs.getInt("intentionalWalks"));
				s.setSteals(rs.getInt("steals"));
				s.setStealsAttempted(rs.getInt("stealsAttempted"));
			}
			rs.close();
			ps.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return s;
	}
	
	public static FieldingStats getFielding(PlayerSeason psi, String pid, Integer yid) {
		FieldingStats s = new FieldingStats();
		try {
			PreparedStatement ps = conn.prepareStatement("select " +
					"sum(E) as errors, " +
					"sum(PO) as putOuts " +
					"from Fielding " +
					"where playerID = ? " + 
					"and yearID = ? ;");
			ps.setString(1, pid);
			ps.setInt(2, yid);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				s.setId(psi);
				s.setErrors(rs.getInt("errors"));
				s.setPutOuts(rs.getInt("putOuts"));
			}
			rs.close();
			ps.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return s;
	}
	
	public static PitchingStats getPitching(PlayerSeason psi, String pid, Integer yid) {
		PitchingStats s = new PitchingStats();
		try {
			PreparedStatement ps = conn.prepareStatement("select " +
					"sum(IPOuts) as outsPitched, " + 
					"sum(ER) as earnedRunsAllowed, " +
					"sum(HR) as homeRunsAllowed, " + 
					"sum(SO) as strikeouts, " +
					"sum(BB) as walks, " + 
					"sum(W) as wins, " +
					"sum(L) as losses, " + 
					"sum(WP) as wildPitches, " +
					"sum(BFP) as battersFaced, " + 
					"sum(HBP) as hitBatters, " +
					"sum(SV) as saves " + 
					"from Pitching " +
					"where playerID = ? " + 
					"and yearID = ? ;");
			ps.setString(1, pid);
			ps.setInt(2, yid);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				s.setId(psi);
				s.setOutsPitched(rs.getInt("outsPitched"));
				s.setEarnedRunsAllowed(rs.getInt("earnedRunsAllowed"));
				s.setHomeRunsAllowed(rs.getInt("homeRunsAllowed"));
				s.setStrikeouts(rs.getInt("strikeouts"));
				s.setWalks(rs.getInt("walks"));
				s.setWins(rs.getInt("wins"));
				s.setLosses(rs.getInt("losses"));
				s.setWildPitches(rs.getInt("wildPitches"));
				s.setBattersFaced(rs.getInt("battersFaced"));
				s.setHitBatters(rs.getInt("hitBatters"));
				s.setSaves(rs.getInt("saves"));
			}
			rs.close();
			ps.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return s;
	}
	
	public static CatchingStats getCatching(PlayerSeason psi, String pid, Integer yid) {
		CatchingStats s = new CatchingStats();
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement("select " +
					"sum(PB) as passedBalls, " +
					"sum(WP) as wildPitches, " +
					"sum(SB) as stealsAllowed, " +
					"sum(CS) as stealsCaught " +
					"from Fielding " +
					"where playerID = ? " + 
					"and yearID = ? ;");
			ps.setString(1, pid);
			ps.setInt(2, yid);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				s.setId(psi);
				s.setPassedBalls(rs.getInt("passedBalls"));
				s.setWildPitches(rs.getInt("wildPitches"));
				s.setStealsAllowed(rs.getInt("stealsAllowed"));
				s.setStealsCaught(rs.getInt("stealsCaught"));
			}
			rs.close();
			ps.close();
		} catch (Exception e) {
			ps.toString();
			e.printStackTrace();
		}
		return s;
	}

	*/
}