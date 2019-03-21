package ui;

import java.lang.reflect.Field;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.*;
import com.sun.jna.platform.win32.COM.Wbemcli;
import com.sun.jna.platform.win32.COM.WbemcliUtil;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.PointerByReference;

import javax.swing.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class ProcessLink implements Comparable<ProcessLink> {
    public Process process;
    public String text;
    public ProcessLink(Process process, String text){
        this.process = process;
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return  false;
        if (!(obj instanceof ProcessLink)) return false;
        ProcessLink other = (ProcessLink)obj;
        return other.process.equals(this.process);
    }
    public int compareTo (ProcessLink otherPlayer) {
        return this.text.compareTo(otherPlayer.text);
    }

    public void BringToFocus(){
        try {
            long pid = getProcessID(process);
            if (process.getClass().getName().equals("java.lang.Win32Process") ||
                    process.getClass().getName().equals("java.lang.ProcessImpl")) {
                showWindowForProcessWindows(pid);
            } else {
                JOptionPane.showMessageDialog(null, "Couldnt show. Process type not supported", "InfoBox: Process not supported", JOptionPane.INFORMATION_MESSAGE);
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    static HashMap<Long, WinDef.HWND> parentToWindows = new HashMap<>();
    static HashMap<WinDef.HWND, Long> windowToParent = new HashMap<>();
    static void showWindowForProcessWindows(long pid){
        final User32 user32 = User32.INSTANCE;
        final Kernel32 kernel32 = Kernel32.INSTANCE;
        if(parentToWindows.containsKey(pid)){
            if(showHwnd(user32, parentToWindows.get(pid)))
                return;
            parentToWindows.clear();
            windowToParent.clear();
        }
        user32.EnumWindows(new WinUser.WNDENUMPROC() {
            int count = 0;
            @Override
            public boolean callback(WinDef.HWND hWnd, Pointer arg1) {
//                long window_hwnd = Pointer.nativeValue(hWnd.getPointer());
                char[] buffer = new char[1024];
                user32.GetWindowText(hWnd, buffer, 1024);
                String title = Native.toString(buffer);
                if(!title.contains("RSPeer"))
                    return true;

                IntByReference pointer = new IntByReference();
                user32.GetWindowThreadProcessId(hWnd, pointer);
                WinNT.HANDLE handle = kernel32.OpenProcess(WinNT.PROCESS_QUERY_INFORMATION | WinNT.PROCESS_VM_READ, false, pointer.getValue());
                long window_pid = kernel32.GetProcessId(handle);
                //long parent_pid = getParentProcessId(window_pid);

                parentToWindows.put(window_pid, hWnd);
                windowToParent.put(hWnd, window_pid);
                //parentToWindows.put(parent_pid, hWnd);
                //windowToParent.put(hWnd, parent_pid);

                if (window_pid == pid)
                    showHwnd(user32, hWnd);
//                else if (parent_pid == pid)
//                    showHwnd(user32, hWnd);

                return true;
            }
        }, null);
    }
    static boolean showHwnd(User32 user32, WinDef.HWND hwnd){
        return user32.SetForegroundWindow(hwnd);
    }
//    static void showWindowForProcessWindows(long pid){
//        final User32 user32 = User32.INSTANCE;
//        final Kernel32 kernel32 = Kernel32.INSTANCE;
//        user32.EnumWindows(new WinUser.WNDENUMPROC() {
//            int count = 0;
//            @Override
//            public boolean callback(WinDef.HWND hWnd, Pointer arg1) {
//                char[] buffer = new char[1024];
//                user32.GetWindowText(hWnd, buffer, 1024);
//                String title = Native.toString(buffer);
//                if(!title.contains("RSPeer"))
//                    return true;
//
//                IntByReference pointer = new IntByReference();
//                user32.GetWindowThreadProcessId(hWnd, pointer);
//                WinNT.HANDLE handle = kernel32.OpenProcess(WinNT.PROCESS_QUERY_INFORMATION | WinNT.PROCESS_VM_READ, false, pointer.getValue());
//                long tmp_pid = kernel32.GetProcessId(handle);
//                if (pid == tmp_pid) {
//                    user32.SetForegroundWindow(hWnd);
//                    return false;
//                }
//                ArrayList<Long> childProcesses = getChildProcessIDs(pid);
//                for(Long child_id : childProcesses){
//                    if(tmp_pid == child_id) {
//                        user32.SetForegroundWindow(hWnd);
//                        return false;
//                    }else{
//
//                    }
//                }
//                return true;
//            }
//        }, null);
//    }
    static HashMap<Long, Long> parentCache = new HashMap<>();
    static Long getParentProcessId(long pid){
        if(parentCache.containsKey(pid)){
            return parentCache.get(pid);
        }
        Wbemcli.IWbemServices svc = null;
        Wbemcli.IEnumWbemClassObject enumRes = null;
        Variant.VARIANT.ByReference pVal = new Variant.VARIANT.ByReference();
        IntByReference pType = new IntByReference();
        IntByReference plFlavor = new IntByReference();
        try{
            svc = WbemcliUtil.connectServer(WbemcliUtil.DEFAULT_NAMESPACE);
            enumRes = svc.ExecQuery("WQL", "SELECT ParentProcessId FROM Win32_Process WHERE ProcessId=" + pid, Wbemcli.WBEM_FLAG_FORWARD_ONLY, null);
            while(true) {
                Wbemcli.IWbemClassObject[] results = enumRes.Next(Wbemcli.WBEM_INFINITE, 100);
                if (results.length == 0) {
                    break;
                }
                for(Wbemcli.IWbemClassObject iwco: results) {
                    // PID is UINT32 = VT_I4
                    iwco.Get("ParentProcessId", 0, pVal, pType, plFlavor);
                    long processId = pVal.longValue();
                    parentCache.put(pid, processId);
                    return processId;
                }
            }
        } finally {
            if (svc != null) svc.Release();
            if (enumRes != null) enumRes.Release();
        }
        return 0L;
    }
//    static HashMap<Long, ArrayList<Long>> childCache = new HashMap<>();
//    static ArrayList<Long> getChildProcessIDs(long pid){
//        if(childCache.containsKey(pid)){
//            return childCache.get(pid);
//        }
//        ArrayList<Long> processIDs = new ArrayList<>();
//        Wbemcli.IWbemServices svc = null;
//        Wbemcli.IEnumWbemClassObject enumRes = null;
//        Variant.VARIANT.ByReference pVal = new Variant.VARIANT.ByReference();
//        IntByReference pType = new IntByReference();
//        IntByReference plFlavor = new IntByReference();
//        try{
//            svc = WbemcliUtil.connectServer(WbemcliUtil.DEFAULT_NAMESPACE);
//            enumRes = svc.ExecQuery("WQL", "SELECT * FROM Win32_Process WHERE ParentProcessId=" + pid, Wbemcli.WBEM_FLAG_FORWARD_ONLY, null);
//            while(true) {
//                Wbemcli.IWbemClassObject[] results = enumRes.Next(Wbemcli.WBEM_INFINITE, 100);
//                if (results.length == 0) {
//                    break;
//                }
//                for(Wbemcli.IWbemClassObject iwco: results) {
//                    // PID is UINT32 = VT_I4
//                    iwco.Get("PROCESSID", 0, pVal, pType, plFlavor);
////                    assertEquals(Wbemcli.CIM_UINT32, pType.getValue());
////                    assertEquals(Variant.VT_I4, pVal.getVarType().intValue());
//                    long processId = pVal.longValue();
//                    processIDs.add(processId);
//                }
//            }
//        } finally {
//            if (svc != null) svc.Release();
//            if (enumRes != null) enumRes.Release();
//        }
//        childCache.put(pid, processIDs);
//        return processIDs;
//    }
    static long getProcessID(Process p)
    {
        long result = -1;
        try
        {
            //for windows
            if (p.getClass().getName().equals("java.lang.Win32Process") ||
                    p.getClass().getName().equals("java.lang.ProcessImpl"))
            {
                Field f = p.getClass().getDeclaredField("handle");
                f.setAccessible(true);
                long handl = f.getLong(p);
                Kernel32 kernel = Kernel32.INSTANCE;
                WinNT.HANDLE hand = new WinNT.HANDLE();
                hand.setPointer(Pointer.createConstant(handl));
                result = kernel.GetProcessId(hand);
                f.setAccessible(false);
            }
            //for unix based operating systems
            else if (p.getClass().getName().equals("java.lang.UNIXProcess"))
            {
                Field f = p.getClass().getDeclaredField("pid");
                f.setAccessible(true);
                result = f.getLong(p);
                f.setAccessible(false);
            }
        }
        catch(Exception ex)
        {
            result = -1;
        }
        return result;
    }
}
