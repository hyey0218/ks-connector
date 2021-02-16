package com.konantech.forensic.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.konantech.connector.dbg.JdbcUtils;
import com.konantech.forensic.common.Const;

//데이터베이스 데이터 접근용 - 공통 쿼리만 포함함
public class DatabaseDao {
	public static final Logger log = LoggerFactory.getLogger(DatabaseDao.class);
	
	public static Map selectThisTaskInfo(String engineId, String taskStatus) {
		// _self references the data source used by this job. 
		DataSource ds = JdbcUtils.getDataSource("MySQL");
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Map result = new HashMap();
		try {
			conn = ds.getConnection();
			String query = "SELECT TASK.*, RESOURCE_PARENT.TASK_ID FROM KONAN_TASK TASK " +
					" LEFT OUTER JOIN KONAN_RESOURCE RESOURCE ON RESOURCE.PARENT_RESOURCE = TASK.RESOURCES_ENGINE " + 
					" LEFT OUTER JOIN KONAN_RESOURCE RESOURCE_PARENT ON RESOURCE_PARENT.RESOURCE_ID = RESOURCE.PARENT_RESOURCE " + 
					" WHERE TASK.STATUS = ? AND TASK.REQUEST_DT IS NOT NULL AND ? IN (TASK.RESOURCES_ENGINE, RESOURCE.RESOURCE_ID) " +
					" AND (RESOURCE_PARENT.TASK_ID IS NULL OR RESOURCE_PARENT.TASK_ID = TASK.TASK_ID) "	+
					//여러 경우에 활용하기 위해, 만일 자원의 부모가 존재하는 경우라면 부모 자원의 TASK_ID 가 TASK 테이블의 TASK_ID와 일치해야 함 
					" ORDER BY TASK.TASK_ID ASC LIMIT 1";
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, taskStatus);
			pstmt.setString(2, engineId);
			//log.info("selectThisTaskInfo pstmt : " + pstmt.toString());
			rs = pstmt.executeQuery();
			if (rs.next()) {
				ResultSetMetaData rsmd = rs.getMetaData();
				for (int i = 1; i <= rsmd.getColumnCount(); i++) {
					result.put(rsmd.getColumnLabel(i), rs.getObject(rsmd.getColumnLabel(i)));
				}
			}
		} catch (Exception e) {
			log.error("selectThisTaskInfo error.", e);
			log.error("pstmt.toString() : " + pstmt.toString());
		} finally {
			JdbcUtils.closeQuietly(rs);
			JdbcUtils.closeQuietly(pstmt);
			JdbcUtils.closeQuietly(conn);
		}	
		return result;
	}
	
	public static List<Map> selectThisScenarioInfo(String scenario) {
		// _self references the data source used by this job. 
		DataSource ds = JdbcUtils.getDataSource("MySQL");
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List result = new ArrayList();
		try {
			conn = ds.getConnection();
			String query = "SELECT * FROM KONAN_TASK_STEP WHERE TASK_STEP_SCENARIO = ? ORDER BY TASK_STEP_ORDER ASC";
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, scenario);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				Map stepInfo = new HashMap();
				ResultSetMetaData rsmd = rs.getMetaData();
				for (int i = 1; i <= rsmd.getColumnCount(); i++) {
					stepInfo.put(rsmd.getColumnLabel(i), rs.getObject(rsmd.getColumnLabel(i)));
				}
				result.add(stepInfo);
			}
		} catch (Exception e) {
			log.error("selectThisScenarioInfo error.", e);
			log.error("pstmt.toString() : " + pstmt.toString());
		} finally {
			JdbcUtils.closeQuietly(rs);
			JdbcUtils.closeQuietly(pstmt);
			JdbcUtils.closeQuietly(conn);
		}	
		return result;
	}

	public static int updateTaskStatus(String taskId, String currentStatus, String targetStatus) {
		// _self references the data source used by this job. 
		DataSource ds = JdbcUtils.getDataSource("MySQL");
		PreparedStatement pstmt = null;
		Connection conn = null;
		int rs = 0;
		try {
			conn = ds.getConnection();
			conn.setAutoCommit(true);
			String query = "UPDATE KONAN_TASK SET STATUS = ?, UP_DT = SYSDATE() WHERE STATUS = ? AND TASK_ID = ?";
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, targetStatus);
			pstmt.setString(2, currentStatus);
			pstmt.setString(3, taskId);

			//ResultSet rs = pstmt.executeQuery();
			rs = pstmt.executeUpdate();
			log.info("updateTaskStatus : " + rs + " records.");
		} catch (Exception e) {
			log.error("updateTaskStatus error.", e);
			log.error("pstmt.toString() : " + pstmt.toString());
		} finally {
			JdbcUtils.closeQuietly(pstmt);
			JdbcUtils.closeQuietly(conn);
		}	
		return rs;
	}

	//해당 엔진 자원  + 볼륨 + 볼륨 캐시 정보 획득
	/**
	 * @param engineResourceId	현재 엔진 자원 ID 
	 * @param volumeStatus		조회할 볼륨 상태
	 * @return
	 */
	public static Map selectAssignedResourcesInfo(String engineResourceId, String volumeStatus) {
		// _self references the data source used by this job. 
		DataSource ds = JdbcUtils.getDataSource("MySQL");
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Map result = new HashMap();
		try {
			conn = ds.getConnection();
			String query = "SELECT ENGINE.*, VOLUME.RESOURCE_ID AS VOLUME_RESOURCE_ID, VOLUME.RESOURCE_ID AS VOLUME_ADDRESS, " +
					" (SELECT RESOURCE_ADDRESS FROM KONAN_RESOURCE WHERE TYPE = ? LIMIT 1) AS VC_ADDRESS " +
					" FROM KONAN_RESOURCE ENGINE " +
					" JOIN KONAN_RESOURCE VOLUME ON VOLUME.ATTACHED_RESOURCE = ENGINE.RESOURCE_ID " +			//볼륨 자원
					" JOIN KONAN_RESOURCE PARENT_VOLUME ON PARENT_VOLUME.RESOURCE_ID = VOLUME.PARENT_RESOURCE " +	// 1단계 부모 볼륨
					" LEFT OUTER JOIN KONAN_RESOURCE PARENT_OF_PARENT_VOLUME " +						// 2단계 부모 볼륨
					"		ON PARENT_OF_PARENT_VOLUME.RESOURCE_ID = PARENT_VOLUME.PARENT_RESOURCE " +
					" WHERE ENGINE.STATUS = ? AND ENGINE.RESOURCE_ID = ? AND VOLUME.STATUS = ? " +
					" AND ((PARENT_VOLUME.STATUS = ? AND PARENT_VOLUME.TASK_ID = ENGINE.TASK_ID) " +	//1~2단계 부모 중에 하나는 해당 작업에 할당된 상태여야 함
					" 	OR (PARENT_OF_PARENT_VOLUME.STATUS = ? AND PARENT_OF_PARENT_VOLUME.TASK_ID = ENGINE.TASK_ID))";
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, Const.TYPE_RESOURCE_VOLUME_CACHE);
			pstmt.setString(2, Const.STATUS_RESOURCE_PROCESSING);
			pstmt.setString(3, engineResourceId);
			pstmt.setString(4, volumeStatus);
			pstmt.setString(5, Const.STATUS_RESOURCE_PROCESSING);
			pstmt.setString(6, Const.STATUS_RESOURCE_PROCESSING);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				ResultSetMetaData rsmd = rs.getMetaData();
				for (int i = 1; i <= rsmd.getColumnCount(); i++) {
					result.put(rsmd.getColumnLabel(i), rs.getObject(rsmd.getColumnLabel(i)));
				}
			}
		} catch (Exception e) {
			log.error("selectAssignedResourcesInfo error.", e);
			log.error("pstmt.toString() : " + pstmt.toString());
		} finally {
			JdbcUtils.closeQuietly(rs);
			JdbcUtils.closeQuietly(pstmt);
			JdbcUtils.closeQuietly(conn);
		}	
		return result;
	}

	public static Map selectThisResourceInfo(String resourceId, String resourceStatus) {
		// _self references the data source used by this job. 
		DataSource ds = JdbcUtils.getDataSource("MySQL");
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Map result = new HashMap();
		try {
			conn = ds.getConnection();
			String query = "SELECT * FROM KONAN_RESOURCE WHERE STATUS = ? AND RESOURCE_ID = ?";
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, resourceStatus);
			pstmt.setString(2, resourceId);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				ResultSetMetaData rsmd = rs.getMetaData();
				for (int i = 1; i <= rsmd.getColumnCount(); i++) {
					result.put(rsmd.getColumnLabel(i), rs.getObject(rsmd.getColumnLabel(i)));
				}
			}
		} catch (Exception e) {
			log.error("selectThisResourceInfo error.", e);
			log.error("pstmt.toString() : " + pstmt.toString());
		} finally {
			JdbcUtils.closeQuietly(rs);
			JdbcUtils.closeQuietly(pstmt);
			JdbcUtils.closeQuietly(conn);
		}	
		return result;
	}

	//해당 포맷의 그룹, 원래 확장자 등 정보 획득
	/**
	 * @param engineResourceId	현재 엔진 자원 ID 
	 * @param volumeStatus		조회할 볼륨 상태
	 * @return
	 */
	public static List<Map> selectTextFilterFormatInfo() {
		// _self references the data source used by this job. 
		DataSource ds = JdbcUtils.getDataSource("MySQL");
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List result = new ArrayList();
		try {
			conn = ds.getConnection();
			String query = "SELECT * FROM KONAN_TEXTFILTER_FORMAT";
			pstmt = conn.prepareStatement(query);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				Map stepInfo = new HashMap();
				ResultSetMetaData rsmd = rs.getMetaData();
				for (int i = 1; i <= rsmd.getColumnCount(); i++) {
					stepInfo.put(rsmd.getColumnLabel(i), rs.getObject(rsmd.getColumnLabel(i)));
				}
				result.add(stepInfo);
			}
		} catch (Exception e) {
			log.error("selectTextFilterFormatInfo error.", e);
			log.error("pstmt.toString() : " + pstmt.toString());
		} finally {
			JdbcUtils.closeQuietly(rs);
			JdbcUtils.closeQuietly(pstmt);
			JdbcUtils.closeQuietly(conn);
		}	
		return result;
	}
}