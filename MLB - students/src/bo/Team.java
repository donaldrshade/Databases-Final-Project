package bo;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity(name = "Teams")
public class Team {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Integer teamId;
	
	@OneToMany(fetch = FetchType.EAGER, cascade=CascadeType.ALL, mappedBy="id.player")
	@Fetch(FetchMode.JOIN)
	Set<TeamSeason> seasons = new HashSet<TeamSeason>();

	@Column
	String name;
	@Column
	String league;
	@Column
	Date year_founded;
	@Column
	Date year_last;

	// utility function
//	public PlayerSeason getPlayerSeason(Integer year) {
//		for (PlayerSeason ps : seasons) {
//			if (ps.getYear().equals(year)) return ps;
//		}
//		return null;
//	}

	
	public Integer getId() {
		return teamId;
	}
	public void setId(Integer id) {
		this.teamId = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	public String getLeague() {
		return league;
	}

	public void setLeague(String league) {
		this.league = league;
	}

	public Date getYearFounded() {
		return year_founded;
	}

	public void setYearFounded(Date yearFounded) {
		this.year_founded = yearFounded;
	}
	public Date getYearLast() {
		return year_last;
	}

	public void setYearLast(Date yearLast) {
		this.year_last = yearLast;
	}


	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Team)){
			return false;
		}
		Team other = (Team) obj;
		return (this.getName().equalsIgnoreCase(other.getName()) &&
				this.getLeague()==other.getLeague() &&
				this.getYearLast()==other.getYearLast() &&
				this.getYearFounded()==other.getYearFounded());
	}
	 
	@Override
	public int hashCode() {
		Integer hash = 0;
		if (this.getName()!=null) hash += this.getName().hashCode(); 
		if (this.getLeague()!=null) hash += this.getLeague().hashCode();
		return hash;
	}
	
	
}
