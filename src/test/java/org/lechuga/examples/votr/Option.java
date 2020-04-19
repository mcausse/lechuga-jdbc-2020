package org.lechuga.examples.votr;

import org.lechuga.anno.Column;
import org.lechuga.anno.Id;

public class Option {

	@Id
	public OptionId id;

	public String tile;
	@Column("descr")
	public String description;

}