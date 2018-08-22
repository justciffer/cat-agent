package com.eazytec.middleware.apm;

import javassist.bytecode.AccessFlag;

public enum AgentModifier{
    PUBLIC() {
        @Override
        public boolean match(Integer modifier) {
            return AccessFlag.isPublic(modifier);
        }
    },PROTECTED {
        @Override
        public boolean match(Integer modifier) {
            return AccessFlag.isProtected(modifier);
        }
    },PRIVATE {
        @Override
        public boolean match(Integer modifier) {
            return AccessFlag.isPrivate(modifier);
        }
    },PACKAGE {
        @Override
        public boolean match(Integer modifier) {
            return AccessFlag.isPackage(modifier);
        }
    };

    public abstract boolean match(Integer modifier);
}