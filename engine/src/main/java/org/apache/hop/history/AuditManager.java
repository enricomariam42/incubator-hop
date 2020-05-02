package org.apache.hop.history;

import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.logging.ILogChannel;
import org.apache.hop.history.local.LocalAuditManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class AuditManager {

  private static AuditManager instance;

  private IAuditManager activeAuditManager;

  private AuditManager() {
    activeAuditManager = new LocalAuditManager();
  }

  public static final AuditManager getInstance() {
    if (instance==null) {
      instance = new AuditManager();
    }
    return instance;
  }

  public static final IAuditManager getActive() {
    return getInstance().getActiveAuditManager();
  }

  /**
   * Gets activeAuditManager
   *
   * @return value of activeAuditManager
   */
  public IAuditManager getActiveAuditManager() {
    return activeAuditManager;
  }

  /**
   * @param activeAuditManager The activeAuditManager to set
   */
  public void setActiveAuditManager( IAuditManager activeAuditManager ) {
    this.activeAuditManager = activeAuditManager;
  }

  // Convenience methods...
  //
  public static final void registerEvent( String group, String type, String name, String operation ) throws HopException {
    getActive().storeEvent( new AuditEvent( group, type, name, operation, new Date() ) );
  }

  public static final List<AuditEvent> findEvents( String group, String type, String operation, int maxNrEvents ) throws HopException {
    List<AuditEvent> events = getActive().findEvents( group, type );

    if ( operation == null ) {
      return events;
    }
    // Filter out the specified operation only (File open)
    //
    List<AuditEvent> operationEvents = new ArrayList<>();
    for ( AuditEvent event : events ) {
      if ( event.getOperation().equalsIgnoreCase( operation ) ) {
        operationEvents.add( event );

        if ( maxNrEvents > 0 && operationEvents.size() >= maxNrEvents ) {
          break;
        }
      }
    }
    return operationEvents;
  }

  public static final void storeState( ILogChannel log, String group, String type, String name, Map<String, Object> stateProperties ) {
    AuditState auditState = new AuditState(name, stateProperties);
    try {
      getActive().storeState( group, type, auditState );
    } catch ( Exception e ) {
      log.logError( "Error writing audit state of type " + type, e );
    }
  }

  public static final AuditState retrieveState( ILogChannel log, String group, String type, String name ) {
    try {
      return getActive().retrieveState( group, type, name );
    } catch(Exception e) {
      log.logError( "Error retrieving state of type "+type );
      return null;
    }
  }
}
