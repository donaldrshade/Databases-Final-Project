package bo;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
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

@Entity(name = "team")
public class Team {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Integer teamId;

	@Column
	String name;
	@Column
	String league;
	@Column
	Date yearFounded;
	@Column
	Date yearLast;

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
		return yearFounded;
	}

	public void setYearFounded(Date yearFounded) {
		this.yearFounded = yearFounded;
	}
	public Date getYearLast() {
		return yearLast;
	}

	public void setYearLast(Date yearLast) {
		this.yearLast = yearLast;
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
		if (this.getYearLast()!=null) hash += this.getYearLast().hashCode();
		if (this.getYearFounded()!=null) hash += this.getYearFounded().hashCode();
		return hash;
	}
	
	
}