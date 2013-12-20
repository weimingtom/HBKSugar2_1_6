LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := crypto

LOCAL_C_INCLUDES := $(LOCAL_PATH)
LOCAL_C_INCLUDES := $(LOCAL_PATH)/include
LOCAL_C_INCLUDES += $(LOCAL_PATH)/crypto 
LOCAL_C_INCLUDES += $(LOCAL_PATH)/crypto/modes 
LOCAL_C_INCLUDES += $(LOCAL_PATH)/crypto/asn1 
LOCAL_C_INCLUDES += $(LOCAL_PATH)/crypto/evp

LOCAL_LDLIBS := -lz

LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/include
LOCAL_EXPORT_LDLIBS := -lz

LOCAL_CFLAGS := -DOPENSSL_THREADS -D_REENTRANT -DDSO_DLFCN -DHAVE_DLFCN_H 

LOCAL_SRC_FILES := 

LOCAL_SRC_FILES += crypto/cryptlib.c crypto/mem.c crypto/mem_dbg.c
LOCAL_SRC_FILES += crypto/cversion.c crypto/ex_data.c crypto/cpt_err.c
LOCAL_SRC_FILES += crypto/ebcdic.c crypto/uid.c crypto/o_time.c
LOCAL_SRC_FILES += crypto/o_str.c crypto/o_dir.c crypto/o_fips.c
LOCAL_SRC_FILES += crypto/o_init.c crypto/fips_ers.c crypto/mem_clr.c

LOCAL_SRC_FILES += crypto/objects/o_names.c crypto/objects/obj_dat.c
LOCAL_SRC_FILES += crypto/objects/obj_lib.c crypto/objects/obj_err.c
LOCAL_SRC_FILES += crypto/objects/obj_xref.c

LOCAL_SRC_FILES += crypto/md4/md4_dgst.c crypto/md4/md4_one.c

LOCAL_SRC_FILES += crypto/md5/md5_dgst.c crypto/md5/md5_one.c

LOCAL_SRC_FILES += crypto/sha/sha_dgst.c crypto/sha/sha1dgst.c 
LOCAL_SRC_FILES += crypto/sha/sha_one.c crypto/sha/sha1_one.c 
LOCAL_SRC_FILES += crypto/sha/sha256.c crypto/sha/sha512.c

LOCAL_SRC_FILES += crypto/mdc2/mdc2dgst.c crypto/mdc2/mdc2_one.c

LOCAL_SRC_FILES += crypto/hmac/hmac.c crypto/hmac/hm_ameth.c crypto/hmac/hm_pmeth.c

LOCAL_SRC_FILES += crypto/ripemd/rmd_dgst.c crypto/ripemd/rmd_one.c 

LOCAL_SRC_FILES += crypto/whrlpool/wp_dgst.c crypto/whrlpool/wp_block.c

LOCAL_SRC_FILES += crypto/des/set_key.c crypto/des/ecb_enc.c
LOCAL_SRC_FILES += crypto/des/cbc_enc.c crypto/des/ecb3_enc.c
LOCAL_SRC_FILES += crypto/des/cfb64enc.c crypto/des/cfb64ede.c
LOCAL_SRC_FILES += crypto/des/cfb_enc.c crypto/des/ofb64ede.c
LOCAL_SRC_FILES += crypto/des/enc_read.c crypto/des/enc_writ.c
LOCAL_SRC_FILES += crypto/des/ofb64enc.c crypto/des/ofb_enc.c
LOCAL_SRC_FILES += crypto/des/str2key.c crypto/des/pcbc_enc.c
LOCAL_SRC_FILES += crypto/des/qud_cksm.c crypto/des/rand_key.c
LOCAL_SRC_FILES += crypto/des/des_enc.c crypto/des/fcrypt_b.c
LOCAL_SRC_FILES += crypto/des/fcrypt.c crypto/des/xcbc_enc.c
LOCAL_SRC_FILES += crypto/des/rpc_enc.c crypto/des/cbc_cksm.c
LOCAL_SRC_FILES += crypto/des/ede_cbcm_enc.c crypto/des/des_old.c
LOCAL_SRC_FILES += crypto/des/des_old2.c crypto/des/read2pwd.c

LOCAL_SRC_FILES += crypto/aes/aes_misc.c crypto/aes/aes_ecb.c
LOCAL_SRC_FILES += crypto/aes/aes_cfb.c crypto/aes/aes_ofb.c
LOCAL_SRC_FILES += crypto/aes/aes_ctr.c crypto/aes/aes_ige.c
LOCAL_SRC_FILES += crypto/aes/aes_wrap.c crypto/aes/aes_core.c
LOCAL_SRC_FILES += crypto/aes/aes_cbc.c

LOCAL_SRC_FILES += crypto/rc2/rc2_ecb.c crypto/rc2/rc2_skey.c 
LOCAL_SRC_FILES += crypto/rc2/rc2_cbc.c crypto/rc2/rc2cfb64.c
LOCAL_SRC_FILES += crypto/rc2/rc2ofb64.c

LOCAL_SRC_FILES += crypto/rc4/rc4_enc.c crypto/rc4/rc4_skey.c
LOCAL_SRC_FILES += crypto/rc4/rc4_utl.c

LOCAL_SRC_FILES += crypto/idea/i_cbc.c crypto/idea/i_cfb64.c 
LOCAL_SRC_FILES += crypto/idea/i_ofb64.c crypto/idea/i_ecb.c 
LOCAL_SRC_FILES += crypto/idea/i_skey.c

LOCAL_SRC_FILES += crypto/bf/bf_skey.c crypto/bf/bf_ecb.c
LOCAL_SRC_FILES += crypto/bf/bf_enc.c crypto/bf/bf_cfb64.c
LOCAL_SRC_FILES += crypto/bf/bf_ofb64.c

LOCAL_SRC_FILES += crypto/cast/c_skey.c crypto/cast/c_ecb.c
LOCAL_SRC_FILES += crypto/cast/c_enc.c crypto/cast/c_cfb64.c
LOCAL_SRC_FILES += crypto/cast/c_ofb64.c

LOCAL_SRC_FILES += crypto/camellia/cmll_ecb.c crypto/camellia/cmll_ofb.c 
LOCAL_SRC_FILES += crypto/camellia/cmll_cfb.c crypto/camellia/cmll_ctr.c
LOCAL_SRC_FILES += crypto/camellia/cmll_utl.c crypto/camellia/camellia.c
LOCAL_SRC_FILES += crypto/camellia/cmll_misc.c crypto/camellia/cmll_cbc.c

LOCAL_SRC_FILES += crypto/seed/seed.c crypto/seed/seed_ecb.c
LOCAL_SRC_FILES += crypto/seed/seed_cbc.c crypto/seed/seed_cfb.c
LOCAL_SRC_FILES += crypto/seed/seed_ofb.c

LOCAL_SRC_FILES += crypto/modes/cbc128.c crypto/modes/ctr128.c 
LOCAL_SRC_FILES += crypto/modes/cts128.c crypto/modes/cfb128.c
LOCAL_SRC_FILES += crypto/modes/ofb128.c crypto/modes/gcm128.c
LOCAL_SRC_FILES += crypto/modes/ccm128.c crypto/modes/xts128.c

LOCAL_SRC_FILES += crypto/bn/bn_add.c crypto/bn/bn_div.c 
LOCAL_SRC_FILES += crypto/bn/bn_exp.c crypto/bn/bn_lib.c 
LOCAL_SRC_FILES += crypto/bn/bn_ctx.c crypto/bn/bn_mul.c 
LOCAL_SRC_FILES += crypto/bn/bn_mod.c crypto/bn/bn_print.c 
LOCAL_SRC_FILES += crypto/bn/bn_rand.c crypto/bn/bn_shift.c 
LOCAL_SRC_FILES += crypto/bn/bn_word.c crypto/bn/bn_blind.c  
LOCAL_SRC_FILES += crypto/bn/bn_kron.c crypto/bn/bn_sqrt.c 
LOCAL_SRC_FILES += crypto/bn/bn_gcd.c crypto/bn/bn_prime.c 
LOCAL_SRC_FILES += crypto/bn/bn_err.c crypto/bn/bn_sqr.c 
LOCAL_SRC_FILES += crypto/bn/bn_asm.c crypto/bn/bn_recp.c 
LOCAL_SRC_FILES += crypto/bn/bn_mont.c crypto/bn/bn_mpi.c 
LOCAL_SRC_FILES += crypto/bn/bn_exp2.c crypto/bn/bn_gf2m.c 
LOCAL_SRC_FILES += crypto/bn/bn_nist.c crypto/bn/bn_depr.c 
LOCAL_SRC_FILES += crypto/bn/bn_const.c crypto/bn/bn_x931p.c

LOCAL_SRC_FILES += crypto/ec/ec_lib.c crypto/ec/ecp_smpl.c 
LOCAL_SRC_FILES += crypto/ec/ecp_mont.c crypto/ec/ecp_nist.c
LOCAL_SRC_FILES += crypto/ec/ec_cvt.c crypto/ec/ec_mult.c
LOCAL_SRC_FILES += crypto/ec/ec_err.c crypto/ec/ec_curve.c
LOCAL_SRC_FILES += crypto/ec/ec_check.c crypto/ec/ec_print.c
LOCAL_SRC_FILES += crypto/ec/ec_asn1.c crypto/ec/ec_key.c
LOCAL_SRC_FILES += crypto/ec/ec2_smpl.c crypto/ec/ec2_mult.c
LOCAL_SRC_FILES += crypto/ec/ec_ameth.c crypto/ec/ec_pmeth.c
LOCAL_SRC_FILES += crypto/ec/eck_prn.c crypto/ec/ecp_nistp224.c
LOCAL_SRC_FILES += crypto/ec/ecp_nistp256.c crypto/ec/ecp_nistp521.c 
LOCAL_SRC_FILES += crypto/ec/ecp_nistputil.c crypto/ec/ecp_oct.c 
LOCAL_SRC_FILES += crypto/ec/ec2_oct.c crypto/ec/ec_oct.c

LOCAL_SRC_FILES += crypto/rsa/rsa_eay.c crypto/rsa/rsa_gen.c
LOCAL_SRC_FILES += crypto/rsa/rsa_lib.c crypto/rsa/rsa_sign.c
LOCAL_SRC_FILES += crypto/rsa/rsa_saos.c crypto/rsa/rsa_err.c
LOCAL_SRC_FILES += crypto/rsa/rsa_pk1.c crypto/rsa/rsa_ssl.c
LOCAL_SRC_FILES += crypto/rsa/rsa_none.c crypto/rsa/rsa_oaep.c
LOCAL_SRC_FILES += crypto/rsa/rsa_chk.c crypto/rsa/rsa_null.c
LOCAL_SRC_FILES += crypto/rsa/rsa_pss.c crypto/rsa/rsa_x931.c
LOCAL_SRC_FILES += crypto/rsa/rsa_asn1.c crypto/rsa/rsa_depr.c
LOCAL_SRC_FILES += crypto/rsa/rsa_ameth.c crypto/rsa/rsa_prn.c
LOCAL_SRC_FILES += crypto/rsa/rsa_pmeth.c crypto/rsa/rsa_crpt.c

LOCAL_SRC_FILES += crypto/dsa/dsa_gen.c crypto/dsa/dsa_key.c
LOCAL_SRC_FILES += crypto/dsa/dsa_lib.c crypto/dsa/dsa_asn1.c
LOCAL_SRC_FILES += crypto/dsa/dsa_vrf.c crypto/dsa/dsa_sign.c
LOCAL_SRC_FILES += crypto/dsa/dsa_err.c crypto/dsa/dsa_ossl.c
LOCAL_SRC_FILES += crypto/dsa/dsa_depr.c crypto/dsa/dsa_ameth.c
LOCAL_SRC_FILES += crypto/dsa/dsa_pmeth.c crypto/dsa/dsa_prn.c

LOCAL_SRC_FILES += crypto/ecdsa/ecs_lib.c crypto/ecdsa/ecs_asn1.c
LOCAL_SRC_FILES += crypto/ecdsa/ecs_ossl.c crypto/ecdsa/ecs_sign.c 
LOCAL_SRC_FILES += crypto/ecdsa/ecs_vrf.c crypto/ecdsa/ecs_err.c

LOCAL_SRC_FILES += crypto/dh/dh_asn1.c crypto/dh/dh_gen.c
LOCAL_SRC_FILES += crypto/dh/dh_key.c crypto/dh/dh_lib.c
LOCAL_SRC_FILES += crypto/dh/dh_check.c crypto/dh/dh_err.c
LOCAL_SRC_FILES += crypto/dh/dh_depr.c crypto/dh/dh_ameth.c
LOCAL_SRC_FILES += crypto/dh/dh_pmeth.c crypto/dh/dh_prn.c

LOCAL_SRC_FILES += crypto/ecdh/ech_lib.c crypto/ecdh/ech_ossl.c
LOCAL_SRC_FILES += crypto/ecdh/ech_key.c crypto/ecdh/ech_err.c

LOCAL_SRC_FILES += crypto/dso/dso_dl.c crypto/dso/dso_dlfcn.c
LOCAL_SRC_FILES += crypto/dso/dso_err.c crypto/dso/dso_lib.c
LOCAL_SRC_FILES += crypto/dso/dso_null.c crypto/dso/dso_openssl.c
LOCAL_SRC_FILES += crypto/dso/dso_win32.c crypto/dso/dso_vms.c
LOCAL_SRC_FILES += crypto/dso/dso_beos.c

LOCAL_SRC_FILES += crypto/engine/eng_err.c crypto/engine/eng_lib.c
LOCAL_SRC_FILES += crypto/engine/eng_list.c crypto/engine/eng_init.c
LOCAL_SRC_FILES += crypto/engine/eng_ctrl.c crypto/engine/eng_table.c
LOCAL_SRC_FILES += crypto/engine/eng_pkey.c crypto/engine/eng_fat.c
LOCAL_SRC_FILES += crypto/engine/eng_all.c crypto/engine/tb_rsa.c
LOCAL_SRC_FILES += crypto/engine/tb_dsa.c crypto/engine/tb_ecdsa.c
LOCAL_SRC_FILES += crypto/engine/tb_dh.c crypto/engine/tb_ecdh.c
LOCAL_SRC_FILES += crypto/engine/tb_rand.c crypto/engine/tb_store.c
LOCAL_SRC_FILES += crypto/engine/tb_cipher.c crypto/engine/tb_digest.c
LOCAL_SRC_FILES += crypto/engine/tb_pkmeth.c crypto/engine/tb_asnmth.c
LOCAL_SRC_FILES += crypto/engine/eng_openssl.c crypto/engine/eng_cnf.c
LOCAL_SRC_FILES += crypto/engine/eng_dyn.c crypto/engine/eng_cryptodev.c
LOCAL_SRC_FILES += crypto/engine/eng_rsax.c crypto/engine/eng_rdrand.c

LOCAL_SRC_FILES += crypto/buffer/buffer.c crypto/buffer/buf_str.c
LOCAL_SRC_FILES += crypto/buffer/buf_err.c

LOCAL_SRC_FILES += crypto/bio/bio_lib.c crypto/bio/bio_cb.c
LOCAL_SRC_FILES += crypto/bio/bio_err.c crypto/bio/bss_mem.c
LOCAL_SRC_FILES += crypto/bio/bss_null.c crypto/bio/bss_fd.c
LOCAL_SRC_FILES += crypto/bio/bss_file.c crypto/bio/bss_sock.c
LOCAL_SRC_FILES += crypto/bio/bss_conn.c crypto/bio/bf_null.c
LOCAL_SRC_FILES += crypto/bio/bf_buff.c crypto/bio/b_print.c
LOCAL_SRC_FILES += crypto/bio/b_dump.c crypto/bio/b_sock.c
LOCAL_SRC_FILES += crypto/bio/bss_acpt.c crypto/bio/bf_nbio.c
LOCAL_SRC_FILES += crypto/bio/bss_log.c crypto/bio/bss_bio.c
LOCAL_SRC_FILES += crypto/bio/bss_dgram.c

LOCAL_SRC_FILES += crypto/stack/stack.c

LOCAL_SRC_FILES += crypto/lhash/lhash.c crypto/lhash/lh_stats.c

LOCAL_SRC_FILES += crypto/rand/md_rand.c crypto/rand/randfile.c 
LOCAL_SRC_FILES += crypto/rand/rand_lib.c crypto/rand/rand_err.c
LOCAL_SRC_FILES += crypto/rand/rand_egd.c crypto/rand/rand_win.c
LOCAL_SRC_FILES += crypto/rand/rand_unix.c crypto/rand/rand_os2.c
LOCAL_SRC_FILES += crypto/rand/rand_nw.c

LOCAL_SRC_FILES += crypto/err/err.c crypto/err/err_all.c
LOCAL_SRC_FILES += crypto/err/err_prn.c

LOCAL_SRC_FILES += crypto/evp/encode.c crypto/evp/digest.c
LOCAL_SRC_FILES += crypto/evp/evp_enc.c crypto/evp/evp_key.c
LOCAL_SRC_FILES += crypto/evp/evp_acnf.c crypto/evp/evp_cnf.c
LOCAL_SRC_FILES += crypto/evp/e_des.c crypto/evp/e_bf.c
LOCAL_SRC_FILES += crypto/evp/e_idea.c crypto/evp/e_des3.c
LOCAL_SRC_FILES += crypto/evp/e_camellia.c crypto/evp/e_rc4.c 
LOCAL_SRC_FILES += crypto/evp/e_aes.c crypto/evp/names.c
LOCAL_SRC_FILES += crypto/evp/e_seed.c crypto/evp/e_xcbc_d.c
LOCAL_SRC_FILES += crypto/evp/e_rc2.c crypto/evp/e_cast.c
LOCAL_SRC_FILES += crypto/evp/e_rc5.c crypto/evp/m_null.c
LOCAL_SRC_FILES += crypto/evp/m_md2.c crypto/evp/m_md4.c
LOCAL_SRC_FILES += crypto/evp/m_md5.c crypto/evp/m_sha.c
LOCAL_SRC_FILES += crypto/evp/m_sha1.c crypto/evp/m_wp.c
LOCAL_SRC_FILES += crypto/evp/m_dss.c crypto/evp/m_dss1.c
LOCAL_SRC_FILES += crypto/evp/m_mdc2.c crypto/evp/m_ripemd.c
LOCAL_SRC_FILES += crypto/evp/m_ecdsa.c crypto/evp/p_open.c
LOCAL_SRC_FILES += crypto/evp/p_seal.c crypto/evp/p_sign.c
LOCAL_SRC_FILES += crypto/evp/p_verify.c crypto/evp/p_lib.c
LOCAL_SRC_FILES += crypto/evp/p_enc.c crypto/evp/p_dec.c
LOCAL_SRC_FILES += crypto/evp/bio_md.c crypto/evp/bio_b64.c
LOCAL_SRC_FILES += crypto/evp/bio_enc.c crypto/evp/evp_err.c
LOCAL_SRC_FILES += crypto/evp/e_null.c crypto/evp/c_all.c
LOCAL_SRC_FILES += crypto/evp/c_allc.c crypto/evp/c_alld.c
LOCAL_SRC_FILES += crypto/evp/evp_lib.c crypto/evp/bio_ok.c
LOCAL_SRC_FILES += crypto/evp/evp_pkey.c crypto/evp/evp_pbe.c
LOCAL_SRC_FILES += crypto/evp/p5_crpt.c crypto/evp/p5_crpt2.c 
LOCAL_SRC_FILES += crypto/evp/e_old.c crypto/evp/pmeth_lib.c
LOCAL_SRC_FILES += crypto/evp/pmeth_fn.c crypto/evp/pmeth_gn.c
LOCAL_SRC_FILES += crypto/evp/m_sigver.c crypto/evp/evp_fips.c
LOCAL_SRC_FILES += crypto/evp/e_aes_cbc_hmac_sha1.c crypto/evp/e_rc4_hmac_md5.c

LOCAL_SRC_FILES += crypto/asn1/a_object.c crypto/asn1/a_bitstr.c
LOCAL_SRC_FILES += crypto/asn1/a_utctm.c crypto/asn1/a_gentm.c
LOCAL_SRC_FILES += crypto/asn1/a_time.c crypto/asn1/a_int.c
LOCAL_SRC_FILES += crypto/asn1/a_octet.c crypto/asn1/a_print.c
LOCAL_SRC_FILES += crypto/asn1/a_type.c crypto/asn1/a_set.c
LOCAL_SRC_FILES += crypto/asn1/a_dup.c crypto/asn1/a_d2i_fp.c
LOCAL_SRC_FILES += crypto/asn1/a_i2d_fp.c crypto/asn1/a_enum.c
LOCAL_SRC_FILES += crypto/asn1/a_utf8.c crypto/asn1/a_sign.c
LOCAL_SRC_FILES += crypto/asn1/a_digest.c crypto/asn1/a_verify.c
LOCAL_SRC_FILES += crypto/asn1/a_mbstr.c crypto/asn1/a_strex.c
LOCAL_SRC_FILES += crypto/asn1/x_algor.c crypto/asn1/x_val.c
LOCAL_SRC_FILES += crypto/asn1/x_pubkey.c crypto/asn1/x_sig.c
LOCAL_SRC_FILES += crypto/asn1/x_req.c crypto/asn1/x_attrib.c
LOCAL_SRC_FILES += crypto/asn1/x_bignum.c crypto/asn1/x_long.c
LOCAL_SRC_FILES += crypto/asn1/x_name.c crypto/asn1/x_x509.c
LOCAL_SRC_FILES += crypto/asn1/x_x509a.c crypto/asn1/x_crl.c
LOCAL_SRC_FILES += crypto/asn1/x_info.c crypto/asn1/x_spki.c
LOCAL_SRC_FILES += crypto/asn1/nsseq.c crypto/asn1/x_nx509.c
LOCAL_SRC_FILES += crypto/asn1/d2i_pu.c crypto/asn1/d2i_pr.c
LOCAL_SRC_FILES += crypto/asn1/i2d_pu.c crypto/asn1/i2d_pr.c
LOCAL_SRC_FILES += crypto/asn1/t_req.c crypto/asn1/t_x509.c
LOCAL_SRC_FILES += crypto/asn1/t_x509a.c crypto/asn1/t_crl.c
LOCAL_SRC_FILES += crypto/asn1/t_pkey.c crypto/asn1/t_spki.c
LOCAL_SRC_FILES += crypto/asn1/t_bitst.c crypto/asn1/tasn_new.c
LOCAL_SRC_FILES += crypto/asn1/tasn_fre.c crypto/asn1/tasn_enc.c
LOCAL_SRC_FILES += crypto/asn1/tasn_dec.c crypto/asn1/tasn_utl.c
LOCAL_SRC_FILES += crypto/asn1/tasn_typ.c crypto/asn1/tasn_prn.c
LOCAL_SRC_FILES += crypto/asn1/ameth_lib.c crypto/asn1/f_int.c
LOCAL_SRC_FILES += crypto/asn1/f_string.c crypto/asn1/n_pkey.c
LOCAL_SRC_FILES += crypto/asn1/f_enum.c crypto/asn1/x_pkey.c
LOCAL_SRC_FILES += crypto/asn1/a_bool.c crypto/asn1/x_exten.c
LOCAL_SRC_FILES += crypto/asn1/bio_asn1.c crypto/asn1/bio_ndef.c
LOCAL_SRC_FILES += crypto/asn1/asn_mime.c crypto/asn1/asn1_gen.c
LOCAL_SRC_FILES += crypto/asn1/asn1_par.c crypto/asn1/asn1_lib.c
LOCAL_SRC_FILES += crypto/asn1/asn1_err.c crypto/asn1/a_bytes.c 
LOCAL_SRC_FILES += crypto/asn1/a_strnid.c crypto/asn1/evp_asn1.c 
LOCAL_SRC_FILES += crypto/asn1/asn_pack.c crypto/asn1/p5_pbe.c
LOCAL_SRC_FILES += crypto/asn1/p5_pbev2.c crypto/asn1/p8_pkey.c
LOCAL_SRC_FILES += crypto/asn1/asn_moid.c

LOCAL_SRC_FILES += crypto/pem/pem_sign.c crypto/pem/pem_seal.c
LOCAL_SRC_FILES += crypto/pem/pem_info.c crypto/pem/pem_lib.c
LOCAL_SRC_FILES += crypto/pem/pem_all.c crypto/pem/pem_err.c
LOCAL_SRC_FILES += crypto/pem/pem_x509.c crypto/pem/pem_xaux.c
LOCAL_SRC_FILES += crypto/pem/pem_oth.c crypto/pem/pem_pk8.c
LOCAL_SRC_FILES += crypto/pem/pem_pkey.c crypto/pem/pvkfmt.c

LOCAL_SRC_FILES += crypto/x509/x509_def.c crypto/x509/x509_d2.c
LOCAL_SRC_FILES += crypto/x509/x509_r2x.c crypto/x509/x509_cmp.c
LOCAL_SRC_FILES += crypto/x509/x509_obj.c crypto/x509/x509_req.c
LOCAL_SRC_FILES += crypto/x509/x509spki.c crypto/x509/x509_vfy.c
LOCAL_SRC_FILES += crypto/x509/x509_set.c crypto/x509/x509cset.c
LOCAL_SRC_FILES += crypto/x509/x509rset.c crypto/x509/x509_err.c
LOCAL_SRC_FILES += crypto/x509/x509name.c crypto/x509/x509_v3.c
LOCAL_SRC_FILES += crypto/x509/x509_ext.c crypto/x509/x509_att.c
LOCAL_SRC_FILES += crypto/x509/x509type.c crypto/x509/x509_lu.c
LOCAL_SRC_FILES += crypto/x509/x_all.c crypto/x509/x509_txt.c
LOCAL_SRC_FILES += crypto/x509/x509_trs.c crypto/x509/by_file.c
LOCAL_SRC_FILES += crypto/x509/by_dir.c crypto/x509/x509_vpm.c

LOCAL_SRC_FILES += crypto/x509v3/v3_bcons.c crypto/x509v3/v3_bitst.c
LOCAL_SRC_FILES += crypto/x509v3/v3_conf.c crypto/x509v3/v3_extku.c
LOCAL_SRC_FILES += crypto/x509v3/v3_ia5.c crypto/x509v3/v3_lib.c
LOCAL_SRC_FILES += crypto/x509v3/v3_prn.c crypto/x509v3/v3_utl.c
LOCAL_SRC_FILES += crypto/x509v3/v3err.c crypto/x509v3/v3_genn.c
LOCAL_SRC_FILES += crypto/x509v3/v3_alt.c crypto/x509v3/v3_skey.c
LOCAL_SRC_FILES += crypto/x509v3/v3_akey.c crypto/x509v3/v3_pku.c
LOCAL_SRC_FILES += crypto/x509v3/v3_int.c crypto/x509v3/v3_enum.c
LOCAL_SRC_FILES += crypto/x509v3/v3_sxnet.c crypto/x509v3/v3_cpols.c
LOCAL_SRC_FILES += crypto/x509v3/v3_crld.c crypto/x509v3/v3_purp.c
LOCAL_SRC_FILES += crypto/x509v3/v3_info.c crypto/x509v3/v3_ocsp.c
LOCAL_SRC_FILES += crypto/x509v3/v3_akeya.c crypto/x509v3/v3_pmaps.c
LOCAL_SRC_FILES += crypto/x509v3/v3_pcons.c crypto/x509v3/v3_ncons.c
LOCAL_SRC_FILES += crypto/x509v3/v3_pcia.c crypto/x509v3/v3_pci.c 
LOCAL_SRC_FILES += crypto/x509v3/pcy_cache.c crypto/x509v3/pcy_node.c
LOCAL_SRC_FILES += crypto/x509v3/pcy_data.c crypto/x509v3/pcy_map.c
LOCAL_SRC_FILES += crypto/x509v3/pcy_tree.c crypto/x509v3/pcy_lib.c
LOCAL_SRC_FILES += crypto/x509v3/v3_asid.c crypto/x509v3/v3_addr.c

LOCAL_SRC_FILES += crypto/conf/conf_err.c crypto/conf/conf_lib.c
LOCAL_SRC_FILES += crypto/conf/conf_api.c crypto/conf/conf_def.c
LOCAL_SRC_FILES += crypto/conf/conf_mod.c crypto/conf/conf_mall.c
LOCAL_SRC_FILES += crypto/conf/conf_sap.c

LOCAL_SRC_FILES += crypto/txt_db/txt_db.c

LOCAL_SRC_FILES += crypto/pkcs7/pk7_asn1.c crypto/pkcs7/pk7_lib.c
LOCAL_SRC_FILES += crypto/pkcs7/pkcs7err.c crypto/pkcs7/pk7_doit.c
LOCAL_SRC_FILES += crypto/pkcs7/pk7_smime.c crypto/pkcs7/pk7_attr.c
LOCAL_SRC_FILES += crypto/pkcs7/pk7_mime.c crypto/pkcs7/bio_pk7.c

LOCAL_SRC_FILES += crypto/pkcs12/p12_add.c crypto/pkcs12/p12_asn.c
LOCAL_SRC_FILES += crypto/pkcs12/p12_attr.c crypto/pkcs12/p12_crpt.c
LOCAL_SRC_FILES += crypto/pkcs12/p12_crt.c crypto/pkcs12/p12_decr.c
LOCAL_SRC_FILES += crypto/pkcs12/p12_init.c crypto/pkcs12/p12_key.c
LOCAL_SRC_FILES += crypto/pkcs12/p12_kiss.c crypto/pkcs12/p12_mutl.c
LOCAL_SRC_FILES += crypto/pkcs12/p12_utl.c crypto/pkcs12/p12_npas.c
LOCAL_SRC_FILES += crypto/pkcs12/pk12err.c crypto/pkcs12/p12_p8d.c
LOCAL_SRC_FILES += crypto/pkcs12/p12_p8e.c

LOCAL_SRC_FILES += crypto/comp/comp_lib.c crypto/comp/comp_err.c
LOCAL_SRC_FILES += crypto/comp/c_rle.c crypto/comp/c_zlib.c

LOCAL_SRC_FILES += crypto/ocsp/ocsp_asn.c crypto/ocsp/ocsp_ext.c
LOCAL_SRC_FILES += crypto/ocsp/ocsp_ht.c crypto/ocsp/ocsp_lib.c
LOCAL_SRC_FILES += crypto/ocsp/ocsp_cl.c crypto/ocsp/ocsp_srv.c
LOCAL_SRC_FILES += crypto/ocsp/ocsp_prn.c crypto/ocsp/ocsp_vfy.c
LOCAL_SRC_FILES += crypto/ocsp/ocsp_err.c

LOCAL_SRC_FILES += crypto/ui/ui_err.c crypto/ui/ui_lib.c
LOCAL_SRC_FILES += crypto/ui/ui_openssl.c crypto/ui/ui_util.c
LOCAL_SRC_FILES += crypto/ui/ui_compat.c

LOCAL_SRC_FILES += crypto/krb5/krb5_asn.c

LOCAL_SRC_FILES += crypto/cms/cms_lib.c crypto/cms/cms_asn1.c
LOCAL_SRC_FILES += crypto/cms/cms_att.c crypto/cms/cms_io.c
LOCAL_SRC_FILES += crypto/cms/cms_smime.c crypto/cms/cms_err.c
LOCAL_SRC_FILES += crypto/cms/cms_sd.c crypto/cms/cms_dd.c
LOCAL_SRC_FILES += crypto/cms/cms_cd.c crypto/cms/cms_env.c
LOCAL_SRC_FILES += crypto/cms/cms_enc.c crypto/cms/cms_ess.c
LOCAL_SRC_FILES += crypto/cms/cms_pwri.c

LOCAL_SRC_FILES += crypto/pqueue/pqueue.c

LOCAL_SRC_FILES += crypto/ts/ts_err.c crypto/ts/ts_req_utils.c
LOCAL_SRC_FILES += crypto/ts/ts_req_print.c crypto/ts/ts_rsp_utils.c
LOCAL_SRC_FILES += crypto/ts/ts_rsp_print.c crypto/ts/ts_rsp_sign.c 
LOCAL_SRC_FILES += crypto/ts/ts_rsp_verify.c crypto/ts/ts_verify_ctx.c
LOCAL_SRC_FILES += crypto/ts/ts_lib.c crypto/ts/ts_conf.c
LOCAL_SRC_FILES += crypto/ts/ts_asn1.c

LOCAL_SRC_FILES += crypto/srp/srp_lib.c crypto/srp/srp_vfy.c

LOCAL_SRC_FILES += crypto/cmac/cmac.c crypto/cmac/cm_ameth.c
LOCAL_SRC_FILES += crypto/cmac/cm_pmeth.c

include $(BUILD_STATIC_LIBRARY)



include $(CLEAR_VARS)

LOCAL_MODULE := ssl

LOCAL_C_INCLUDES := $(LOCAL_PATH)
LOCAL_C_INCLUDES := $(LOCAL_PATH)/include
LOCAL_C_INCLUDES += $(LOCAL_PATH)/crypto 
LOCAL_C_INCLUDES += $(LOCAL_PATH)/crypto/modes 
LOCAL_C_INCLUDES += $(LOCAL_PATH)/crypto/asn1 
LOCAL_C_INCLUDES += $(LOCAL_PATH)/crypto/evp

LOCAL_LDLIBS := -lz

LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/include
LOCAL_EXPORT_LDLIBS := -lz

LOCAL_SRC_FILES :=
LOCAL_SRC_FILES += ssl/s2_meth.c ssl/s2_srvr.c
LOCAL_SRC_FILES += ssl/s2_clnt.c ssl/s2_lib.c
LOCAL_SRC_FILES += ssl/s2_enc.c ssl/s2_pkt.c
LOCAL_SRC_FILES += ssl/s3_meth.c ssl/s3_srvr.c
LOCAL_SRC_FILES += ssl/s3_clnt.c ssl/s3_lib.c
LOCAL_SRC_FILES += ssl/s3_enc.c ssl/s3_pkt.c
LOCAL_SRC_FILES += ssl/s3_both.c ssl/s3_cbc.c
LOCAL_SRC_FILES += ssl/s23_meth.c ssl/s23_srvr.c
LOCAL_SRC_FILES += ssl/s23_clnt.c ssl/s23_lib.c
LOCAL_SRC_FILES += ssl/s23_pkt.c ssl/t1_meth.c
LOCAL_SRC_FILES += ssl/t1_srvr.c ssl/t1_clnt.c
LOCAL_SRC_FILES += ssl/t1_lib.c ssl/t1_enc.c
LOCAL_SRC_FILES += ssl/d1_meth.c ssl/d1_srvr.c
LOCAL_SRC_FILES += ssl/d1_clnt.c ssl/d1_lib.c
LOCAL_SRC_FILES += ssl/d1_pkt.c ssl/d1_both.c
LOCAL_SRC_FILES += ssl/d1_enc.c ssl/d1_srtp.c
LOCAL_SRC_FILES += ssl/ssl_lib.c ssl/ssl_err2.c
LOCAL_SRC_FILES += ssl/ssl_cert.c ssl/ssl_sess.c
LOCAL_SRC_FILES += ssl/ssl_ciph.c ssl/ssl_stat.c
LOCAL_SRC_FILES += ssl/ssl_rsa.c ssl/ssl_asn1.c
LOCAL_SRC_FILES += ssl/ssl_txt.c ssl/ssl_algs.c
LOCAL_SRC_FILES += ssl/bio_ssl.c ssl/ssl_err.c
LOCAL_SRC_FILES += ssl/kssl.c ssl/tls_srp.c
LOCAL_SRC_FILES += ssl/t1_reneg.c

include $(BUILD_STATIC_LIBRARY)
