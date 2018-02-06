/*
 * Copyright (C) 2004-2017 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jserver.gameserver.dao.impl.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2jserver.commons.database.pool.impl.ConnectionFactory;
import com.l2jserver.gameserver.dao.HennaDAO;
import com.l2jserver.gameserver.data.xml.impl.HennaData;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.items.L2Henna;

/**
 * Henna DAO MySQL implementation.
 * @author Zoey76
 */
public class HennaDAOMySQLImpl implements HennaDAO
{
	private static final Logger LOG = LoggerFactory.getLogger(HennaDAOMySQLImpl.class);
	
	private static final String SELECT = "SELECT slot,symbol_id FROM character_hennas WHERE charId=? AND class_index=?";
	private static final String INSERT = "INSERT INTO character_hennas (charId,symbol_id,slot,class_index) VALUES (?,?,?,?)";
	private static final String DELETE_ONE = "DELETE FROM character_hennas WHERE charId=? AND slot=? AND class_index=?";
	private static final String DELETE_ALL = "DELETE FROM character_hennas WHERE charId=? AND class_index=?";
	
	@Override
	public void load(L2PcInstance player)
	{
		try (Connection con = ConnectionFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(SELECT))
		{
			ps.setInt(1, player.getObjectId());
			ps.setInt(2, player.getClassIndex());
			try (ResultSet rset = ps.executeQuery())
			{
				final L2Henna[] henna = new L2Henna[3];
				while (rset.next())
				{
					int slot = rset.getInt("slot");
					if ((slot < 1) || (slot > 3))
					{
						continue;
					}
					
					int symbolId = rset.getInt("symbol_id");
					if (symbolId == 0)
					{
						continue;
					}
					henna[slot - 1] = HennaData.getInstance().getHenna(symbolId);
				}
				player.setHenna(henna);
			}
		}
		catch (Exception e)
		{
			LOG.error("Failed restoing character {} hennas. {}", player, e);
		}
	}
	
	@Override
	public void insert(L2PcInstance player, L2Henna henna, int slot)
	{
		try (Connection con = ConnectionFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(INSERT))
		{
			ps.setInt(1, player.getObjectId());
			ps.setInt(2, henna.getDyeId());
			ps.setInt(3, slot);
			ps.setInt(4, player.getClassIndex());
			ps.execute();
		}
		catch (Exception e)
		{
			LOG.error("Failed saving character henna. {}", e);
		}
	}
	
	@Override
	public void delete(L2PcInstance player, int slot)
	{
		try (Connection con = ConnectionFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(DELETE_ONE))
		{
			ps.setInt(1, player.getObjectId());
			ps.setInt(2, slot + 1);
			ps.setInt(3, player.getClassIndex());
			ps.execute();
		}
		catch (Exception e)
		{
			LOG.error("Failed removing character henna. {}", e);
		}
	}
	
	@Override
	public void deleteAll(L2PcInstance player, int classIndex)
	{
		try (Connection con = ConnectionFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(DELETE_ALL))
		{
			ps.setInt(1, player.getObjectId());
			ps.setInt(2, classIndex);
			ps.execute();
		}
		catch (Exception e)
		{
			LOG.error("Failed removing character henna. {}", e);
		}
	}
}
